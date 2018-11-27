Public Class Bcd

    Private Shared serialVersionUID As Long

    Private value() As Byte

    Public Sub New(ByVal bcdBytes() As Byte)
        MyBase.New
        Me.value = bcdBytes
    End Sub

    Public Function getBytes() As Byte()
        Return Me.value
    End Function

    Public Overloads Function toString() As String
        Dim bytes() As Byte = New Byte(((Me.value.Length * 2)) - 1) {}
        Dim c As Integer = 0
        If ((Me.value((Me.value.Length - 1)) And 240) _
                    = 240) Then
            bytes(c = c + 1) = 45
        Else
            bytes(c = c + 1) = CType((((Me.value((Me.value.Length - 1)) + 4) _
                        And 15) _
                        + 48), Byte)
        End If

        bytes(c = c + 1) = CType(((Me.value((Me.value.Length - 1)) And 15) _
                    + 48), Byte)
        Dim i As Integer = (Me.value.Length - 2)
        Do While (i >= 0)
            bytes(c = c + 1) = CType((((Me.value(i) + 4) _
                        And 15) _
                        + 48), Byte)
            bytes(c = c + 1) = CType(((Me.value(i) And 15) _
                        + 48), Byte)
            i = (i - 1)
        Loop

        Return bytes.ToString
    End Function

    Public Overloads Function doubleValue() As Double
        Return Me.longValue
    End Function

    Public Overloads Function floatValue() As Single
        Return Me.longValue
    End Function

    Public Overloads Function intValue() As Integer
        Return CType(Me.longValue, Integer)
    End Function

    Public Overloads Function longValue() As Long
        Dim result As Long
        Dim factor As Long
        Dim i As Integer = 0
        Do While (i _
                    < (Me.value.Length - 1))
            result = (result _
                        + ((Me.value(i) And 15) _
                        * factor))
            result = (result _
                        + (((Me.value(i) + 4) _
                        And 15) _
                        * factor))
            i = (i + 1)
        Loop

        result = (result _
                    + ((Me.value((Me.value.Length - 1)) And 15) _
                    * factor))
        If ((Me.value((Me.value.Length - 1)) And 240) _
                    = 240) Then
            result = (result * -1)
        Else
            result = (result _
                        + (((Me.value((Me.value.Length - 1)) + 4) _
                        And 15) _
                        * factor))
        End If

        Return result
    End Function
End Class
