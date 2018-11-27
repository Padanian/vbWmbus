Public Class EncryptionModes

    Public Enum EncryptionMode As Integer
        NONE = 0
        AES_128 = 1
        DES_CBC = 2
        DES_CBC_IV = 3
        RESERVED_04 = 4
        AES_CBC_IV = 5
        RESERVED_06 = 6
        AES_CBC_IV_0 = 7
        RESERVED_08 = 8
        RESERVED_09 = 9
        RESERVED_10 = 10
        RESERVED_11 = 11
        RESERVED_12 = 12
        TLS = 13
        RESERVED_14 = 14
        RESERVED_15 = 15
    End Enum

End Class
