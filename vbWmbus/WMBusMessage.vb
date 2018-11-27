Imports System.Text

Public Class WMBusMessage

    Private signalStrengthInDBm As Integer

    Private buffer() As Byte

    Private controlField As Integer

    Private secondaryAddress As SecondaryAddress

    Private vdr As VariableDataStructure

    Private Sub New(ByVal signalStrengthInDBm As Integer, ByVal buffer() As Byte, ByVal controlField As Integer, ByVal secondaryAddress As SecondaryAddress, ByVal vdr As VariableDataStructure)
        MyBase.New
        Me.signalStrengthInDBm = Me.signalStrengthInDBm
        Me.buffer = Me.buffer
        Me.controlField = Me.controlField
        Me.secondaryAddress = Me.secondaryAddress
        Me.vdr = Me.vdr
    End Sub

    Private Shared Function decode(ByVal buffer() As Byte, ByVal signalStrengthInDBm As Integer, ByVal keyMap As Dictionary(Of SecondaryAddress, Byte())) As WMBusMessage
        Dim length As Integer = (buffer(0) And 255)
        If (length > (buffer.Length - 1)) Then
            Dim msg As String = Format("Byte buffer has only a length of " & buffer.Length.ToString & " while the specified length field is " & length.ToString)
            Throw New Exception(msg)
        End If

        Dim controlField As Integer = (buffer(1) And 255)
        Dim secondaryAddress As SecondaryAddress = SecondaryAddress.newFromWMBusLlHeader(buffer, 2)
        Dim vdr As VariableDataStructure = New VariableDataStructure(buffer, 10, (length - 9), secondaryAddress, keyMap)
        Return New WMBusMessage(signalStrengthInDBm, buffer, controlField, secondaryAddress, vdr)
    End Function

    Public Function asBlob() As Byte()
        Return Me.buffer
    End Function

    Public Function getControlField() As Integer
        Return Me.controlField
    End Function

    Public Function getSecondaryAddress() As SecondaryAddress
        Return Me.secondaryAddress
    End Function

    Public Function getVariableDataResponse() As VariableDataStructure
        Return Me.vdr
    End Function

    Public Function getRssi() As Integer
        Return Me.signalStrengthInDBm
    End Function

    Public Overrides Function toString() As String
        Dim builder As StringBuilder = New StringBuilder
        If Not signalStrengthInDBm = 0 Then
            builder.Append("Message was received with signal strength: ").Append(Me.signalStrengthInDBm).Append("dBm" & vbLf)
        End If

        Return builder.Append("Control Field: ").Append(String.Format("0x%02X", Me.controlField)).Append("" & vbLf & "Secondary Address -> ").Append(Me.secondaryAddress).Append("" & vbLf & "Variable Data Response:" & vbLf).Append(Me.vdr).ToString
    End Function
End Class
