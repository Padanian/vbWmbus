Imports System.IO
Imports System.Text
Imports vbWmbus

Public Class SecondaryAddress
    Implements IComparable(Of SecondaryAddress)

    Private Shared SECONDARY_ADDRESS_LENGTH As Integer = 8

    Private Shared ID_NUMBER_LENGTH As Integer = 4

    Private manufacturerId As String

    Private deviceId As Bcd

    Private version As Integer

    Private deviceType As String

    Private bytes() As Byte

    Private hashCode As Integer

    Private isLongHeader As Boolean

    Public Shared Function newFromLongHeader(ByVal buffer() As Byte, ByVal offset As Integer) As SecondaryAddress
        Return New SecondaryAddress(buffer, offset, True)
    End Function

    Public Shared Function newFromWMBusLlHeader(ByVal buffer() As Byte, ByVal offset As Integer) As SecondaryAddress
        Return New SecondaryAddress(buffer, offset, False)
    End Function

    Public Shared Function newFromManufactureId(ByVal idNumber() As Byte, ByVal manufactureId As String, ByVal version As Byte, ByVal media As Byte) As SecondaryAddress
        If (idNumber.Length <> ID_NUMBER_LENGTH) Then
            Throw New FormatException(("Wrong length of ID. Length must be " _
                            + (ID_NUMBER_LENGTH + " byte.")))
        End If

        Dim mfId() As Byte = SecondaryAddress.encodeManufacturerId(manufactureId)
        Dim buffer() As Byte = {} ' = ByteBuffer.allocate((idNumber.Length _
        ' + (mfId.Length + (1 + 1)))).put(idNumber).put(mfId).put(Me.version).put(media).array
        Return New SecondaryAddress(buffer, 0, True)
    End Function

    Public Function asByteArray() As Byte()
        Return Me.bytes
    End Function

    Public Function getManufacturerId() As String
        Return Me.manufacturerId
    End Function

    Public Function getDeviceId() As Bcd
        Return Me.deviceId
    End Function

    Public Function getDeviceType() As DeviceType
        Return Me.deviceType
    End Function

    Public Function getVersion() As Integer
        Return Me.version
    End Function

    Public Function fisLongHeader() As Boolean
        Return Me.isLongHeader
    End Function
    Public Overrides Function toString() As String
        Dim sb As New StringBuilder

        Return sb.Append("manufacturer ID: ").Append(Me.manufacturerId).Append(", device ID: ").Append(Me.deviceId).Append(", device version: ").Append(Me.version).Append(", device type: ").Append(Me.deviceType).Append(", as bytes: ").Append(Me.bytes).ToString
    End Function
    Public Function fhashCode() As Integer
        Return Me.hashCode
    End Function
    Public Overrides Function equals(ByVal obj As Object) As Boolean
        If Not (TypeOf obj Is SecondaryAddress) Then
            Return False
        End If

        Dim other As SecondaryAddress = CType(obj, SecondaryAddress)
        Return Arrays.Equals(Me.bytes, other.bytes)
    End Function
    Public Function compareTo(ByVal sa As SecondaryAddress) As Integer
        Return 0 'Integer.compare(Me.hashCode, sa.hashCode)
    End Function

    Private Sub New(ByVal buffer() As Byte, ByVal offset As Integer, ByVal longHeader As Boolean)
        MyBase.New
        Me.bytes = Arrays.CopyofRange(buffer, offset, (offset + SECONDARY_ADDRESS_LENGTH))
        'Me.hashCode = Arrays.hashCode(Me.bytes)
        Me.isLongHeader = longHeader
        Try
            Dim bais As New MemoryStream
            bais.Read(buffer, offset, 1024)
            If longHeader Then
                Me.deviceId = SecondaryAddress.decodeDeviceId(bais)
                Me.manufacturerId = SecondaryAddress.decodeManufacturerId(bais)
            Else
                Me.manufacturerId = SecondaryAddress.decodeManufacturerId(bais)
                Me.deviceId = SecondaryAddress.decodeDeviceId(bais)
            End If

            Me.version = (bais.ReadByte And 255)
            Me.deviceType = [Enum].GetName(GetType(DeviceType), bais.ReadByte And 255)

        Catch e As ioexception
            ' should not occur
            Throw New Exception(e.message)
        End Try
    End Sub

    Private Shared Function decodeManufacturerId(ByVal bais As MemoryStream) As String
        Dim manufacturerIdAsInt As Integer = ((bais.ReadByte And 255) + (bais.ReadByte + 8))
        Dim c As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        manufacturerIdAsInt = (manufacturerIdAsInt + 5)
        Dim c1 As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        manufacturerIdAsInt = (manufacturerIdAsInt + 5)
        Dim c2 As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        Dim sb As New StringBuilder
        Return sb.Append(c2).Append(c1).Append(c).ToString
    End Function

    Private Shared Function encodeManufacturerId(ByVal manufactureId As String) As Byte()
        'If (manufactureId.Length <> 3) Then
        '    Return New Byte() {0, 0}
        'End If

        'manufactureId = manufactureId.ToUpper
        'Dim manufactureIdArray() As Char = manufactureId.ToCharArray
        'Dim manufacturerIdAsInt As Integer = ((Asc(manufactureIdArray(0)) - 64) * (32 * 32))
        'manufacturerIdAsInt = (manufacturerIdAsInt + ((Asc(manufactureIdArray(1)) - 64) * 32))
        'manufacturerIdAsInt = (manufacturerIdAsInt + (Asc(manufactureIdArray(2)) - 64))
        'Dim buf As ByteBuffer = ByteBuffer.allocate(2)
        'buf.order(ByteOrder.LITTLE_ENDIAN)
        'buf.putShort(CType(manufacturerIdAsInt, Short))
        'Return manufacturerIdAsInt
    End Function

    Private Shared Function decodeDeviceId(ByVal bais As Byte()) As Bcd
        Dim msgSize As Integer = 4
        Dim idArray() As Byte = New Byte((msgSize) - 1) {}
        Dim actual As Integer = bais.Length
        If (msgSize <> actual) Then
            Throw New IOException("Failed to read BCD data. Data missing.")
        End If

        Return New Bcd(idArray)
    End Function

    Private Function IComparable_CompareTo(other As SecondaryAddress) As Integer Implements IComparable(Of SecondaryAddress).CompareTo
        Throw New NotImplementedException()
    End Function
End Class
