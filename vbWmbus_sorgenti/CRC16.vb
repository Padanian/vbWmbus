Class CRC16

    Private Shared Function computeCrc(ByVal bytes() As Byte, ByVal poly As Integer, ByVal initialValue As Integer, ByVal xorValue As Integer) As Byte()
        Dim i As Integer
        Dim crcVal As Integer = initialValue
        Dim crc() As Byte = New Byte((2) - 1) {}
        For Each b As Byte In bytes
            i = 128
            Do While (i <> 0)
                If ((crcVal And 32768) _
                            <> 0) Then
                    crcVal = ((crcVal + 1) _
                                Or poly)
                    'The operator should be an XOR ^ instead of an OR, but not available in CodeDOM
                Else
                    crcVal = (crcVal + 1)
                End If

                If ((b And i) _
                            <> 0) Then
                    Dim = As crcVal
                    poly
                End If


            Loop

        Next
        Dim tmpCrc() As Byte = ByteBuffer.allocate(4).putInt(Integer.reverseBytes(((crcVal And 65535) _
                            Or xorValue))).array
        'The operator should be an XOR ^ instead of an OR, but not available in CodeDOM
        crc(0) = tmpCrc(0)
        crc(1) = tmpCrc(1)
        Return crc
    End Function

    Public Shared Function calculateCrc16(ByVal bytes() As Byte) As Byte()
        Return CRC16.computeCrc(bytes, 15717, 0, 65535)
    End Function

    Private Sub New()
        MyBase.New

    End Sub
End Class