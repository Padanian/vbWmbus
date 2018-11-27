Public Class VariableDataStructure


    Private buffer() As Byte

    Private offset As Integer

    Private length As Integer

    Private header() As Byte = New Byte((0) - 1) {}

    Private linkLayerSecondaryAddress As SecondaryAddress

    Private keyMap As Dictionary(Of SecondaryAddress, Byte())

    Private secondaryAddress As SecondaryAddress

    Private accessNumber As Integer

    Private status As Integer

    Private communicationControl As Byte

    Private sessionNumber() As Byte

    Private encryptionMode As EncryptionModes.EncryptionMode

    Private numberOfEncryptedBlocks As Integer

    Private manufacturerData() As Byte = New Byte((0) - 1) {}

    Private vdr() As Byte = New Byte((0) - 1) {}

    Private moreRecordsFollow As Boolean = False

    Private decoded As Boolean = False

    Private dataRecords As List(Of DataRecord)

    Public Sub New(ByVal buffer() As Byte, ByVal offset As Integer, ByVal length As Integer, ByVal linkLayerSecondaryAddress As SecondaryAddress, ByVal keyMap As Map(Of SecondaryAddress, Byte()))
        MyBase.New
        Me.buffer = Me.buffer
        Me.offset = Me.offset
        Me.length = Me.length
        Me.linkLayerSecondaryAddress = Me.linkLayerSecondaryAddress
        Me.keyMap = Me.keyMap
        Me.dataRecords = New List(Of DataRecord)
    End Sub
    Public Sub decode()
        If Not Me.decoded Then
            Try
                Dim ciField As Integer = VariableDataStructure.readUnsignedByte(Me.buffer, Me.offset)
                Select Case (ciField)
                    Case 114
                        Me.decodeLongHeaderData()
                    Case 120
                        Me.encryptionMode = EncryptionModes.EncryptionMode.NONE
                        Me.decodeDataRecords(Me.buffer, (Me.offset + 1), (Me.length - 1))
                    Case 122
                        Me.decodeWithShortHeader()
                    Case 141
                        Me.decodeExtendedLinkLayer(Me.buffer, (Me.offset + 1))
                        ' 6 bytes header + CRC
                        Me.header = Arrays.CopyofRange(Me.buffer, Me.offset, (Me.offset + 7))
                        ' don't include CRC
                        Me.vdr = New Byte(((Me.length - 7)) - 1) {}
                        Array.Copy(Me.buffer, (Me.offset + 7), Me.vdr, 0, (Me.length - 7))
                        If Me.Equals(EncryptionModes.EncryptionMode.AES_128) Then
                            Me.decryptMessage(Me.getKey)
                        End If

                        If ((Me.vdr(2) And 255) _
                                    = 120) Then
                            Me.decodeDataRecords(Me.vdr, 3, (Me.length - 10))
                        ElseIf ((Me.vdr(2) And 255) _
                                    = 121) Then
                            Me.decodeShortFrame(Me.vdr, 3, (Me.length - 10))
                        End If

                    Case 51
                        Dim msg As String = String.Format("Received telegram with CI 0x33. Decoding not implemented. Device Serial: %s, Manufacturer: %s.", Me.linkLayerSecondaryAddress.getDeviceId.toString, Me.linkLayerSecondaryAddress.getManufacturerId)
                        Throw New Exception(msg)
                    Case Else
                        Dim strFormat As String = "Unable to decode message with this CI Field: 0x%02X."
                        If ciField >= &HA0 And ciField <= &HB7 Then
                            strFormat = "Manufacturer specific CI: 0x%02X."
                        End If
                        Throw New Exception(String.Format(strFormat, ciField))
                End Select

            Catch e As Exception
                Throw New Exception(e.Message)
            End Try

            Me.decoded = True
        End If

    End Sub

    Private Sub decodeWithShortHeader()
        decodeShortHeader(Me.buffer, (Me.offset + 1))
        Select Case Me.encryptionMode
            Case EncryptionModes.EncryptionMode.NONE
                Me.decodeDataRecords(Me.buffer, (Me.offset + 5), (Me.length - 5))
            Case EncryptionModes.EncryptionMode.AES_CBC_IV
                Me.decryptAesCbcIv(Me.buffer, (Me.offset + 5), (Me.numberOfEncryptedBlocks * 16))
            Case EncryptionModes.EncryptionMode.AES_128, EncryptionModes.EncryptionMode.AES_CBC_IV_0,
                 EncryptionModes.EncryptionMode.DES_CBC, EncryptionModes.EncryptionMode.DES_CBC_IV,
                 EncryptionModes.EncryptionMode.RESERVED_04, EncryptionModes.EncryptionMode.RESERVED_06,
                 EncryptionModes.EncryptionMode.RESERVED_08, EncryptionModes.EncryptionMode.RESERVED_09,
                 EncryptionModes.EncryptionMode.RESERVED_10, EncryptionModes.EncryptionMode.RESERVED_11,
                 EncryptionModes.EncryptionMode.RESERVED_12, EncryptionModes.EncryptionMode.RESERVED_14,
                 EncryptionModes.EncryptionMode.RESERVED_15, EncryptionModes.EncryptionMode.TLS
            Case Else
                Throw New Exception(("Unsupported encryption mode used: " + Me.encryptionMode))
        End Select

    End Sub

    Private Overloads Sub decryptAesCbcIv(ByVal buffer() As Byte, ByVal offset As Integer, ByVal encryptedDataLength As Integer)
        Me.vdr = New Byte((encryptedDataLength) - 1) {}
        Array.Copy(Me.buffer, Me.offset, Me.vdr, 0, encryptedDataLength)
        Dim key() As Byte = Me.keyMap(Me.linkLayerSecondaryAddress)
        If (key Is Nothing) Then
            Dim msg As String = Format("Unable to decode encrypted payload. " & vbLf & "Secondary address key was not registered: " & vbLf & Me.linkLayerSecondaryAddress.toString)
            Throw New Exception(msg)
        End If

        Me.decodeDataRecords(Me.decryptMessage(key), 0, encryptedDataLength)
    End Sub

    Private Sub decodeLongHeaderData()
        Dim headerLength As Integer = 13
        Me.header = Arrays.CopyofRange(Me.buffer, Me.offset, (Me.offset + headerLength))
        Me.secondaryAddress = SecondaryAddress.newFromLongHeader(Me.buffer, (Me.offset + 1))
        Me.decodeShortHeader(Me.buffer, (Me.offset + (1 + 8)))
        Me.vdr = New Byte(((Me.length - headerLength)) - 1) {}
        Array.Copy(Me.buffer, (Me.offset + headerLength), Me.vdr, 0, (Me.length - headerLength))
        Select Case (Me.encryptionMode)
            Case EncryptionModes.EncryptionMode.NONE
                ' nothing to do
            Case EncryptionModes.EncryptionMode.AES_CBC_IV
                Me.decryptMessage(Me.getKey)
            Case EncryptionModes.EncryptionMode.AES_128, EncryptionModes.EncryptionMode.AES_CBC_IV_0,
                 EncryptionModes.EncryptionMode.DES_CBC, EncryptionModes.EncryptionMode.DES_CBC_IV,
                 EncryptionModes.EncryptionMode.RESERVED_04, EncryptionModes.EncryptionMode.RESERVED_06,
                 EncryptionModes.EncryptionMode.RESERVED_08, EncryptionModes.EncryptionMode.RESERVED_09,
                 EncryptionModes.EncryptionMode.RESERVED_10, EncryptionModes.EncryptionMode.RESERVED_11,
                 EncryptionModes.EncryptionMode.RESERVED_12, EncryptionModes.EncryptionMode.RESERVED_14,
                 EncryptionModes.EncryptionMode.RESERVED_15, EncryptionModes.EncryptionMode.TLS
            Case Else
                Throw New Exception(("Unsupported encryption mode used: " + Me.encryptionMode))
        End Select

        Me.decodeDataRecords(Me.vdr, 0, (Me.length - headerLength))
    End Sub

    Public Function getSecondaryAddress() As SecondaryAddress
        Return Me.secondaryAddress
    End Function

    Public Function getAccessNumber() As Integer
        Return Me.accessNumber
    End Function

    Public Function getEncryptionMode() As EncryptionModes.EncryptionMode
        Return Me.encryptionMode
    End Function

    Public Function getManufacturerData() As Byte()
        Return Me.manufacturerData
    End Function

    Public Function getNumberOfEncryptedBlocks() As Integer
        Return Me.numberOfEncryptedBlocks
    End Function

    Public Function getStatus() As Integer
        Return Me.status
    End Function

    Public Function getDataRecords() As List(Of DataRecord)
        Return Me.dataRecords
    End Function

    Public Function m_moreRecordsFollow() As Boolean
        Return Me.moreRecordsFollow
    End Function

    Private Sub decodeExtendedLinkLayer(ByVal buffer() As Byte, ByVal offset As Integer)
        Dim i As Integer = Me.offset
        Me.communicationControl = Me.buffer(i + +)
        Me.accessNumber = Me.buffer(i + +)
        Me.sessionNumber = New Byte() {Me.buffer(i + +), Me.buffer(i + +), Me.buffer(i + +), Me.buffer(i + +)}
        Me.encryptionMode = EncryptionModes.EncryptionMode.GetName(GetType(EncryptionModes.EncryptionMode), (Me.sessionNumber(3) + 5))
        Dim checksum() As Byte = New Byte() {Me.buffer(i + +), Me.buffer(i + +)}
        Dim crc() As Byte = CRC16.calculateCrc16(Arrays.CopyofRange(Me.buffer, i, (Me.buffer.Length - 1)))
        If ((checksum(0) = crc(0)) _
                    AndAlso (checksum(1) = crc(1))) Then
            Me.encryptionMode = EncryptionModes.EncryptionMode.NONE
        End If

    End Sub

    Private Sub decodeShortHeader(ByVal buffer() As Byte, ByVal offset As Integer)
        Dim i As Integer = Me.offset
        Me.accessNumber = VariableDataStructure.readUnsignedByte(Me.buffer, i + +)
        Me.status = VariableDataStructure.readUnsignedByte(Me.buffer, i + +)
        Me.numberOfEncryptedBlocks = ((Me.buffer(i + +) And 240) _
                    + 4)
        Me.encryptionMode = encryptionMode.getInstance((Me.buffer(i + +) And 15))
        If VariableDataStructure.msgIsNotEnc(Me.buffer, i) Then
            Me.encryptionMode = encryptionMode.NONE
        End If

    End Sub

    Private Shared Function msgIsNotEnc(ByVal buffer() As Byte, ByVal i As Integer) As Boolean
        Dim b As Byte = Me.buffer(i)
        Dim b2 As Byte = Me.buffer((i + 1))
        Return ((b = CType(47, Byte)) _
                    AndAlso (b2 = CType(2, Byte)))
    End Function

    Private Shared Function readUnsignedByte(ByVal msg() As Byte, ByVal i As Integer) As Integer
        Return (msg(i) And 255)
    End Function

    Public Function getHeader() As Byte()
        Return Me.header
    End Function

    Private Sub decodeDataRecords(ByVal buffer() As Byte, ByVal offset As Integer, ByVal length As Integer)
        Dim i As Integer = Me.offset

        While (i _
                    < (Me.offset _
                    + (Me.length - 2)))
            If ((Me.buffer(i) And 239) _
                        = 15) Then
                ' manufacturer specific data
                Me.moreRecordsFollow = ((Me.buffer(i) And 16) _
                            = 16)
                Me.manufacturerData = Arrays.CopyofRange(Me.buffer, (i + 1), (Me.offset _
                                + (Me.length - 2)))
                Return
            End If

            If (Me.buffer(i) = 47) Then
                ' this is a fill byte because some encryption mechanisms need multiples of 8 bytes to encode data
                i = (i + 1)
                'TODO: Warning!!! continue If
            End If

            Dim dataRecord As DataRecord = New DataRecord
            i = dataRecord.decode(Me.buffer, i)
            Me.dataRecords.Add(dataRecord)

        End While

        If (Not (Me.linkLayerSecondaryAddress) Is Nothing) Then
            deviceHistory.put(Me.linkLayerSecondaryAddress, Me.dataRecords)
        End If

    End Sub

    Private Sub decodeShortFrame(ByVal data() As Byte, ByVal offset As Integer, ByVal length As Integer)
        If Not deviceHistory.containsKey(Me.linkLayerSecondaryAddress) Then
            deviceHistory.put(Me.linkLayerSecondaryAddress, New LinkedList(Of DataRecord))
        End If

        Dim buf As ByteBuffer = ByteBuffer.wrap(data, Me.offset, Me.length)
        buf.order(ByteOrder.nativeOrder)
        ' skip checksum data
        buf.position(4)
        Me.dataRecords = deviceHistory.get(Me.linkLayerSecondaryAddress)
        Dim iter As ListIterator(Of DataRecord) = Me.dataRecords.listIterator

        While iter.hasNext
            Dim dr As DataRecord = iter.next
            Try
                ByteArrayOutputStream
            End Try

            os = New ByteArrayOutputStream
            os.write(dr.getDib)
            os.write(dr.getVib)
            Dim tempOffset As Integer = (dr.getDib.Length + dr.getVib.Length)
            ' This might not be right
            Dim dataLegth As Integer = (tempOffset + dr.getDataLength)
            Dim b() As Byte = New Byte(((dataLegth - tempOffset)) - 1) {}
            buf.get(b)
            os.write(b)
            Dim newDataRecord As DataRecord = New DataRecord
            newDataRecord.decode(os.toByteArray, 0)
            iter.set(newDataRecord)
            IOException
            e
            ' ignore

        End While

    End Sub

    Public Function decryptMessage(ByVal key() As Byte) As Byte()
        If (Me.encryptionMode = encryptionMode.NONE) Then
            Return Me.vdr
        End If

        If (key Is Nothing) Then
            Throw New DecodingException("AES key for given address not specified.")
        End If

        Dim len As Integer = (Me.numberOfEncryptedBlocks * 16)
        If (len > Me.vdr.Length) Then
            Throw New DecodingException("Number of encrypted exceeds payload size!")
        End If

        Select Case (Me.encryptionMode)
            Case AES_CBC_IV
                Me.decryptAesCbcIv(key, len)
            Case AES_128
                Me.decryptAes128(key, len)
            Case Else
                Throw New DecodingException(("Unsupported encryption mode: " + Me.encryptionMode))
        End Select

        Return Me.vdr
    End Function

    Private Sub decryptAes128(ByVal key() As Byte, ByVal len As Integer)
        Dim iv() As Byte = Me.createIvKamstrup
        Dim result() As Byte = AesCrypt.newAesCtrCrypt(key, iv).decrypt(Me.vdr, len)
        Dim crc() As Byte = CRC16.calculateCrc16(Arrays.CopyofRange(result, 2, result.Length))
        If ((result(0) <> crc(0)) _
                    OrElse (result(1) <> crc(1))) Then
            Throw New DecodingException(Me.newDecyptionExceptionMsg)
        End If

        Me.vdr = result
    End Sub

    Private Overloads Sub decryptAesCbcIv(ByVal key() As Byte, ByVal len As Integer)
        Dim iv() As Byte = Me.createIv
        Dim result() As Byte = AesCrypt.newAesCrypt(key, iv).decrypt(Me.vdr, len)
        If Not ((result(0) = 47) _
                    AndAlso (result(1) = 47)) Then
            Throw New DecodingException(Me.newDecyptionExceptionMsg)
        End If

        System.arraycopy(result, 0, Me.vdr, 0, len)
    End Sub

    Private Function newDecyptionExceptionMsg() As String
        Dim deviceId As String = Me.linkLayerSecondaryAddress.getDeviceId.toString
        Dim manId As String = Me.linkLayerSecondaryAddress.getManufacturerId
        Return String.Format("%s - %s - Decryption unsuccessful! Wrong AES/CTR Key?", deviceId, manId)
    End Function

    Private Function createIv() As Byte()
        Dim iv() As Byte = New Byte((16) - 1) {}
        Dim saBytes() As Byte = Me.linkLayerSecondaryAddress.asByteArray
        If Me.linkLayerSecondaryAddress.isLongHeader Then
            System.arraycopy(saBytes, 0, iv, 4, 2)
            ' Manufacture
            System.arraycopy(saBytes, 2, iv, 0, 4)
            ' Identification
            System.arraycopy(saBytes, 6, iv, 6, 2)
            ' Version and Device Type
        Else
            System.arraycopy(saBytes, 0, iv, 0, 8)
        End If

        Dim i As Integer = 8
        Do While (i < iv.Length)
            iv(i) = CType(Me.accessNumber, Byte)
            i = (i + 1)
        Loop

        Return iv
    End Function

    Private Function createIvKamstrup() As Byte()
        Try
            ByteArrayOutputStream
        End Try

        os = New ByteArrayOutputStream
        os.write(Me.linkLayerSecondaryAddress.asByteArray, 0, 8)
        os.write(communicationControl, &, ', (1 + 4))
        os.write(Me.sessionNumber)
        os.write(New Byte((3) - 1) {})
        ' 3 * 0x00
        Return os.toByteArray
        IOException
        e
        Throw New DecodingException("Unable to create initial vector for decryption.", e)
    End Function

    Private Function getKey() As Byte()
        Dim key() As Byte = Me.keyMap.get(Me.linkLayerSecondaryAddress)
        If (Not (key) Is Nothing) Then
            Return key
        End If

        Dim msg As String = ("Unable to decode encrypted payload because no key for the following secondary address was registered:" &
        " " + Me.linkLayerSecondaryAddress)
        Throw New DecodingException(msg)
    End Function

    <Override()>
    Public Function toString() As String
        Dim builder As StringBuilder = New StringBuilder
        If Not Me.decoded Then
            If Me.dataRecords.isEmpty Then
                Dim from As Integer = Me.offset
                Dim to As Integer = (from + Me.length)
                Dim hexString As String = DatatypeConverter.printHexBinary(Arrays.CopyofRange(Me.buffer, from, to))
                Return MessageFormat.format("VariableDataResponse has not been decoded. Bytes:" & vbLf&"{0}", hexString)
            Else
                builder.append(("VariableDataResponse has not been fully decoded. " _
                                + (Me.dataRecords.size + " data records decoded." & vbLf)))
            End If

        End If

        If (Not (Me.secondaryAddress) Is Nothing) Then
            builder.append("Secondary address: {").append(Me.secondaryAddress).append("}" & vbLf)
        End If

        builder.append("Short Header: {Access No.: ").append(Me.accessNumber).append(", status: ").append(Me.status).append(", encryption mode: ").append(Me.encryptionMode).append(", number of encrypted blocks: ").append(Me.numberOfEncryptedBlocks).append("}")
        For Each dataRecord As DataRecord In Me.dataRecords
            builder.append("" & vbLf).append(dataRecord.ToString)
        Next
        If (Me.manufacturerData.Length <> 0) Then
            Dim manDaraHexStr As String = DatatypeConverter.printHexBinary(Me.manufacturerData)
            builder.append("" & vbLf&"Manufacturer specific bytes:"& vbLf).append(manDaraHexStr)
        End If

        If Me.moreRecordsFollow Then
            builder.append("" & vbLf&"More records follow ...")
        End If

        Return builder.toString
    End Function
End Class