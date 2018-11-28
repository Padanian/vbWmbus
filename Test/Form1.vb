Imports vbWmbusNameSpace.vbWmbus

Public Class Form1


    Private Sub Button1_Click(sender As Object, e As EventArgs) Handles Button1.Click

        Dim add As New vbWmbusNameSpace.vbWmbus
        Dim de = add.MainVBWMBUS("2844C5149310504301167210010000C51400162B2B00202F2F046D18295C2B0413F900000001FD1758005000")

        RichTextBox1.Text = de.Item2

    End Sub
End Class