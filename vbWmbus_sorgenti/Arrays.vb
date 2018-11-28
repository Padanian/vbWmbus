Imports System.Runtime.CompilerServices

Module Arrays

    <Extension()>
    Public Function CopyofRange(ByVal arra As Byte(), FromCopy As Integer, ToCopy As Integer) As Byte()
        Dim arrb As Byte() = {}
        Array.Resize(arrb, ToCopy - FromCopy)
        Array.Copy(arra, FromCopy, arrb, 0, ToCopy - FromCopy)
        Return arrb
    End Function

End Module
