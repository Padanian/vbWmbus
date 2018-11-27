Imports System.Text

Public Class DataRecord

    Public Enum DataValueType
        _LONG
        _DOUBLE
        _DATE
        _STRING
        BCD
        NONE

    End Enum
    Public Enum FunctionField
        INST_VAL
        MAX_VAL
        MIN_VAL
        ERROR_VAL
    End Enum
    Public Enum Description

        ENERGY
        VOLUME
        MASS
        ON_TIME
        OPERATING_TIME
        POWER
        VOLUME_FLOW
        VOLUME_FLOW_EXT
        MASS_FLOW
        FLOW_TEMPERATURE
        RETURN_TEMPERATURE
        TEMPERATURE_DIFFERENCE
        EXTERNAL_TEMPERATURE
        PRESSURE
        DDATE
        DATE_TIME
        VOLTAGE
        CURRENT
        AVERAGING_DURATION
        ACTUALITY_DURATION
        FABRICATION_NO
        MODEL_VERSION
        PARAMETER_SET_ID
        HARDWARE_VERSION
        FIRMWARE_VERSION
        ERROR_FLAGS
        CUSTOMER
        RESERVED
        OPERATING_TIME_BATTERY
        RF_LEVEL
        HCA
        REACTIVE_ENERGY
        TEMPERATURE_LIMIT
        MAX_POWER
        REACTIVE_POWER
        REL_HUMIDITY
        FREQUENCY
        PHASE
        EXTENDED_IDENTIFICATION
        ADDRESS
        NOT_SUPPORTED
        MANUFACTURER_SPECIFIC
        FUTURE_VALUE
        USER_DEFINED
        APPARENT_ENERGY
        CUSTOMER_LOCATION
        ACCESS_CODE_OPERATOR
        ACCESS_CODE_USER
        PASSWORD
        ACCESS_CODE_SYSTEM_DEVELOPER
        OTHER_SOFTWARE_VERSION
        ACCESS_CODE_SYSTEM_OPERATOR
        ERROR_MASK
        SECURITY_KEY
        DIGITAL_INPUT
        BAUDRATE
        DIGITAL_OUTPUT
        RESPONSE_DELAY_TIME
        RETRY
        FIRST_STORAGE_NUMBER_CYCLIC
        REMOTE_CONTROL
        LAST_STORAGE_NUMBER_CYCLIC
        SIZE_STORAGE_BLOCK
        STORAGE_INTERVALL
        TARIF_START
        DURATION_LAST_READOUT
        TIME_POINT
        TARIF_DURATION
        OPERATOR_SPECIFIC_DATA
        TARIF_PERIOD
        NUMBER_STOPS
        LAST_CUMULATION_DURATION
        SPECIAL_SUPPLIER_INFORMATION
        PARAMETER_ACTIVATION_STATE
        CONTROL_SIGNAL
        WEEK_NUMBER
        DAY_OF_WEEK
        REMAINING_BATTERY_LIFE_TIME
        TIME_POINT_DAY_CHANGE
        CUMULATION_COUNTER
        RESET_COUNTER

    End Enum

    ' // Data Information Block that contains a DIF and optionally up to 10 DIFEs
    Private dib() As Byte

    ' // Value Information Block that contains a VIF and optionally up to 10 VIFEs
    Private vib() As Byte

    Private dataValue As Object

    Private m_dataValueType As DataValueType

    ' DIB fields:
    Private m_functionField As FunctionField

    Private storageNumber As Long

    ' max is 41 bits
    Private tariff As Integer

    ' max 20 bits
    Private subunit As Short

    ' max 10 bits
    ' VIB fields:
    Private m_description As Description

    Private userDefinedDescription As String

    Private multiplierExponent As Integer = 0

    Private unit As DlmsUnits.DlmsUnit

    Private dateTypeF As Boolean = False

    Private dateTypeG As Boolean = False

    Private dataLength As Integer

    Private Function decode(ByVal buffer() As Byte, ByVal offset As Integer) As Integer
        Dim i As Integer = offset
        Me.decodeDib(buffer, i)
        Dim dataField As Integer = (buffer(i) And 15)
        Me.dataLength = dataField
        Me.storageNumber = ((buffer(i) And 64) + 6)
        Me.subunit = 0
        Me.tariff = 0
        Dim numDife As Integer = 0

        While ((buffer(i = i + 1) And 128) = 128)
            Me.subunit = (Me.subunit + (((buffer(i) And 64) + 6) + numDife))
            Me.tariff = (Me.tariff + (((buffer(i) And 48) + 4) + (numDife * 2)))
            Me.storageNumber = (Me.storageNumber + ((buffer(i) And 15) + ((numDife * 4) + 1)))
            numDife = (numDife + 1)

        End While

        Me.multiplierExponent = 0
        Me.unit = Nothing
        Me.dib = Arrays.CopyofRange(buffer, offset, i)
        ' decode VIB
        Dim vif As Integer = (buffer(i = i + 1) And 255)
        Dim decodeFurtherVifs As Boolean = False
        If (vif = 251) Then
            Me.decodeAlternateExtendedVif(buffer(i))
            If ((buffer(i) And 128) = 128) Then
                decodeFurtherVifs = True
            End If

            i = (i = i + 1)
        ElseIf ((vif And 127) = 124) Then
            i = (i + Me.decodeUserDefinedVif(buffer, i))
            If ((vif And 128) = 128) Then
                decodeFurtherVifs = True
            End If

        ElseIf (vif = 253) Then
            Me.decodeMainExtendedVif(buffer(i))
            If ((buffer(i) And 128) = 128) Then
                decodeFurtherVifs = True
            End If

            i = (i = i + 1)
        Else
            Me.decodeMainVif(vif)
            If ((vif And 128) = 128) Then
                decodeFurtherVifs = True
            End If

        End If

        If decodeFurtherVifs Then

            While ((buffer(i = i + 1) And 128) = 128)
                ' TODO these vifes should not be ignored!

            End While

        End If

        Me.vib = Arrays.CopyofRange(buffer, (offset + Me.dib.Length), i)
        Select Case (dataField)
            Case 0, 8
                Me.dataValue = Nothing
                Me.m_dataValueType = DataValueType.NONE
            Case 1
                Me.dataValue = CLng(buffer(i = i + 1))
                Me.m_dataValueType = DataValueType._LONG
            Case 2
                If Me.dateTypeG Then
                    Dim day As Integer = (31 And buffer(i))
                    Dim year1 As Integer = ((224 And buffer(i = i + 1)) + 5)
                    Dim month As Integer = (15 And buffer(i))
                    Dim year2 As Integer = ((240 And buffer(i = i + 1)) + 1)
                    Dim year As Integer = (2000 + (year1 + year2))
                    Dim calendar As DateTime = New DateTime(year, (month - 1), day, 0, 0, 0)
                    Me.dataValue = calendar.TimeOfDay
                    Me.m_dataValueType = DataValueType._DATE
                Else
                    Me.dataValue = CLng(((buffer(i = i + 1) And 255) Or ((buffer(i = i + 1) And 255) + 8)))
                    Me.m_dataValueType = DataValueType._LONG
                End If

            Case 3
                If ((buffer((i + 2)) And 128) = 128) Then
                    ' negative
                    Me.dataValue = CLng((((buffer(i = i + 1) And 255) Or
                        (((buffer(i = i + 1) And 255) + 8) Or
                        (((buffer(i = i + 1) And 255) + 16) Or 255))) + 24))
                Else
                    Me.dataValue = CLng(((buffer(i = i + 1) And 255) Or
                        (((buffer(i = i + 1) And 255) + 8) Or
                        ((buffer(i = i + 1) And 255) + 16))))
                End If

                Me.m_dataValueType = DataValueType._LONG
            Case 4
                If Me.dateTypeF Then
                    Dim min As Integer = (buffer(i = i + 1) And 63)
                    Dim hour As Integer = (buffer(i) And 31)
                    Dim yearh As Integer = ((96 And buffer(i = i + 1)) + 5)
                    Dim day As Integer = (buffer(i) And 31)
                    Dim year1 As Integer = ((224 And buffer(i = i + 1)) + 5)
                    Dim mon As Integer = (buffer(i) And 15)
                    Dim year2 As Integer = ((240 And buffer(i = i + 1)) + 1)
                    If (yearh = 0) Then
                        yearh = 1
                    End If

                    Dim year As Integer = (1900 + ((100 * yearh) + (year1 + year2)))
                    Dim calendar As DateTime = New DateTime(year, (mon - 1), day, hour, min, 0)
                    Me.dataValue = calendar.TimeOfDay
                    Me.m_dataValueType = DataValueType._DATE
                Else
                    Me.dataValue = CLng((buffer(i = i + 1) And 255) Or
                        ((buffer(i = i + 1) And 255) + 8) Or
                        (buffer(i = i + 1) And 255) Or ((buffer(i = i + 1) And 255) + 24))
                    Me.m_dataValueType = DataValueType._LONG
                End If

            Case 5
                'Dim doubleDatavalue As Double = ByteBuffer.wrap(buffer, i, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat
                'i = (i + 4)
                'Me.dataValue = CDbl(doubleDatavalue)
                'Me.m_dataValueType = DataValueType._DOUBLE
            Case 6
                If ((buffer((i + 5)) And 128) = 128) Then
                    ' negative
                    dataValue = CLng((buffer(i = i + 1) And 255) Or
                        (((buffer(i = i + 1) And 255) + 8)) Or
                        (((buffer(i = i + 1) And 255) + 16)) Or
                        (((buffer(i = i + 1) And 255) + 24)) Or
                        (((CType(buffer(i = i + 1), Long) And 255) + 32)) Or
                        (((CType(buffer(i = i + 1), Long) And 255) + 40)) Or (255 << 48) Or (256 << 56))
                Else
                    Me.dataValue = CLng(((buffer(i = i + 1) And 255) Or
                        (((buffer(i = i + 1) And 255) + 8) Or
                        (((buffer(i = i + 1) And 255) + 16) Or
                        (((buffer(i = i + 1) And 255) + 24) Or
                        (((CType(buffer(i = i + 1), Long) And 255) + 32) Or
                        ((CType(buffer(i = i + 1), Long) And 255) + 40)))))))
                End If

                Me.m_dataValueType = DataValueType._LONG
            Case 7
                Me.dataValue = CLng(((buffer(i = i + 1) And 255) Or (((buffer(i = i + 1) And 255) + 8) _
                                Or (((buffer(i = i + 1) And 255) + 16) _
                                Or (((buffer(i = i + 1) And 255) + 24) _
                                Or (((CType(buffer(i = i + 1), Long) And 255) + 32) _
                                Or (((CType(buffer(i = i + 1), Long) And 255) + 40) _
                                Or (((CType(buffer(i = i + 1), Long) And 255) + 48) _
                                Or ((CType(buffer(i = i + 1), Long) And 255) + 56)))))))))
                Me.m_dataValueType = DataValueType._LONG
            Case 9
                i = Me.setBCD(buffer, i, 1)
            Case 10
                i = Me.setBCD(buffer, i, 2)
            Case 11
                i = Me.setBCD(buffer, i, 3)
            Case 12
                i = Me.setBCD(buffer, i, 4)
            Case 14
                i = Me.setBCD(buffer, i, 6)
            Case 13
                Dim variableLength As Integer = (buffer(i = i + 1) And 255)
                Dim dataLength0x0d As Integer
                If (variableLength < 192) Then
                    dataLength0x0d = variableLength
                ElseIf ((variableLength >= 192) AndAlso (variableLength <= 201)) Then
                    dataLength0x0d = (2 * (variableLength - 192))
                ElseIf ((variableLength >= 208) AndAlso (variableLength <= 217)) Then
                    dataLength0x0d = (2 * (variableLength - 208))
                ElseIf ((variableLength >= 224) AndAlso (variableLength <= 239)) Then
                    dataLength0x0d = (variableLength - 224)
                ElseIf (variableLength = 248) Then
                    dataLength0x0d = 4
                Else
                    Throw New Exception(("Unsupported LVAR Field: " + variableLength))
                End If


                Dim rawData() As Byte = New Byte((dataLength0x0d) - 1) {}
                Dim j As Integer = 0
                Do While (j < dataLength0x0d)
                    rawData(j) = buffer((i + (dataLength0x0d - (1 - j))))
                    j = (j + 1)
                Loop

                i = (i + dataLength0x0d)
                Me.dataValue = rawData.ToString
                Me.m_dataValueType = DataValueType._STRING
            Case Else
                Dim msg As String = String.Format("Unknown Data Field in DIF: %02X.", dataField)
                Throw New Exception(msg)
        End Select

        Return i
    End Function
    Private Function setBCD(ByVal buffer() As Byte, ByVal i As Integer, ByVal j As Integer) As Integer
        Me.dataValue = New Bcd(Arrays.CopyofRange(buffer, i, (i + j)))
        Me.m_dataValueType = DataValueType.BCD
        Return (i + j)
    End Function
    Private Sub decodeDib(ByVal buffer() As Byte, ByVal i As Integer)
        Dim ff As Integer = ((buffer(i) And 48) + 4)
        Select Case (ff)
            Case 0
                Me.m_functionField = FunctionField.INST_VAL
            Case 1
                Me.m_functionField = FunctionField.MAX_VAL
            Case 2
                Me.m_functionField = FunctionField.MIN_VAL
            Case 3
                Me.m_functionField = FunctionField.ERROR_VAL
            Case Else
                Me.m_functionField = Nothing
        End Select

    End Sub
    Private Function encode(ByVal buffer() As Byte, ByVal offset As Integer) As Integer
        Dim i As Integer = offset
        Array.Copy(Me.dib, 0, buffer, i, Me.dib.Length)
        i = (i + Me.dib.Length)
        Array.Copy(Me.vib, 0, buffer, i, Me.vib.Length)
        i = (i + Me.vib.Length)
        Return (i - offset)
    End Function
    Public Function getDib() As Byte()
        Return Me.dib
    End Function
    Public Function getVib() As Byte()
        Return Me.vib
    End Function
    Public Function getDataValue() As Object
        Return Me.dataValue
    End Function
    Public Function getDataValueType() As DataValueType
        Return Me.m_dataValueType
    End Function
    Public Function getScaledDataValue() As Double
        Try
            Return (CType(Me.dataValue, Double) * Math.Pow(10, Me.multiplierExponent))
        Catch e As Exception
            Return Nothing
        End Try

    End Function
    Public Function getFunctionField() As FunctionField
        Return Me.m_functionField
    End Function
    Public Function getStorageNumber() As Long
        Return Me.storageNumber
    End Function
    Public Function getTariff() As Integer
        Return Me.tariff
    End Function
    Public Function getSubunit() As Short
        Return Me.subunit
    End Function
    Public Function getDescription() As Description
        Return Me.m_description
    End Function
    Public Function getUserDefinedDescription() As String
        If (Me.m_description = Description.USER_DEFINED) Then
            Return Me.userDefinedDescription
        Else
            Return Me.m_description.ToString
        End If

    End Function
    Public Function getMultiplierExponent() As Integer
        Return Me.multiplierExponent
    End Function
    Public Function getUnit() As DlmsUnits.DlmsUnit
        Return Me.unit
    End Function
    Private Sub decodeTimeUnit(ByVal vif As Integer)
        If ((vif And 2) _
                    = 0) Then
            If ((vif And 1) _
                        = 0) Then
                Me.unit = DlmsUnits.DlmsUnit.SECOND
            Else
                Me.unit = DlmsUnits.DlmsUnit.MIN
            End If

        ElseIf ((vif And 1) _
                    = 0) Then
            Me.unit = DlmsUnits.DlmsUnit.HOUR
        Else
            Me.unit = DlmsUnits.DlmsUnit.DAY
        End If

    End Sub
    Private Function decodeUserDefinedVif(ByVal buffer() As Byte, ByVal offset As Integer) As Integer
        Dim length As Integer = buffer(offset)
        Dim sb As StringBuilder = New StringBuilder
        Dim i As Integer = (offset + length)
        Do While (i > offset)
            sb.Append(buffer(i).ToString)
            i = (i - 1)
        Loop

        Me.m_description = Description.USER_DEFINED
        Me.userDefinedDescription = sb.ToString
        Return (length + 1)
    End Function
    Private Sub decodeMainVif(ByVal vif As Integer)
        Me.m_description = Description.NOT_SUPPORTED
        If ((vif And 64) = 0) Then
            Me.decodeE0(vif)
        Else
            Me.decodeE1(vif)
        End If

    End Sub
    Private Sub decodeE1(ByVal vif As Integer)
        ' E1
        If ((vif And 32) = 0) Then
            Me.decodeE10(vif)
        Else
            Me.decodeE11(vif)
        End If

    End Sub
    Private Sub decodeE11(ByVal vif As Integer)
        ' E11
        If ((vif And 16) = 0) Then
            Me.decodeE110(vif)
        Else
            Me.decodeE111(vif)
        End If

    End Sub
    Private Sub decodeE111(ByVal vif As Integer)
        ' E111
        If ((vif And 8) = 0) Then
            ' E111 0
            If ((vif And 4) = 0) Then
                Me.m_description = Description.AVERAGING_DURATION
            Else
                Me.m_description = Description.ACTUALITY_DURATION
            End If

            Me.decodeTimeUnit(vif)
        Else
            ' E111 1
            If ((vif And 4) = 0) Then
                ' E111 10
                If ((vif And 2) = 0) Then
                    ' E111 100
                    If ((vif And 1) = 0) Then
                        ' E111 1000
                        Me.m_description = Description.FABRICATION_NO
                    Else
                        ' E111 1001
                        Me.m_description = Description.EXTENDED_IDENTIFICATION
                    End If

                Else
                    ' E111 101
                    If ((vif And 1) = 0) Then
                        Me.m_description = Description.ADDRESS
                    Else
                        ' E111 1011
                        ' Codes used with extension indicator 0xFB (table 29 of DIN EN 13757-3:2011)
                        Throw New Exception("Trying to decode a mainVIF even though it is an alternate extended vif")
                    End If

                End If

            Else
                ' E111 11
                If ((vif And 2) = 0) Then
                    ' E111 110
                    If ((vif And 1) = 0) Then
                        ' E111 1100
                        ' Extension indicator 0xFC: VIF is given in following string
                        Me.m_description = Description.NOT_SUPPORTED
                    Else
                        ' E111 1101
                        ' Extension indicator 0xFD: main VIFE-code extension table (table 28 of DIN EN
                        ' 13757-3:2011)
                        Throw New Exception("Trying to decode a mainVIF even though it is a main extended vif")
                    End If

                Else
                    ' E111 111
                    If ((vif And 1) = 0) Then
                        ' E111 1110
                        Me.m_description = Description.FUTURE_VALUE
                    Else
                        ' E111 1111
                        Me.m_description = Description.MANUFACTURER_SPECIFIC
                    End If

                End If

            End If

        End If

    End Sub
    Private Sub decodeE110(ByVal vif As Integer)
        ' E110
        If ((vif And 8) = 0) Then
            ' E110 0
            If ((vif And 4) = 0) Then
                ' E110 00
                Me.m_description = Description.TEMPERATURE_DIFFERENCE
                Me.multiplierExponent = ((vif And 3) - 3)
                Me.unit = DlmsUnits.DlmsUnit.KELVIN
            Else
                ' E110 01
                Me.m_description = Description.EXTERNAL_TEMPERATURE
                Me.multiplierExponent = ((vif And 3) - 3)
                Me.unit = DlmsUnits.DlmsUnit.DEGREE_CELSIUS
            End If

        Else
            ' E110 1
            If ((vif And 4) = 0) Then
                ' E110 10
                Me.m_description = Description.PRESSURE
                Me.multiplierExponent = ((vif And 3) - 3)
                Me.unit = DlmsUnits.DlmsUnit.BAR
            Else
                ' E110 11
                If ((vif And 2) = 0) Then
                    ' E110 110
                    If ((vif And 1) = 0) Then
                        ' E110 1100
                        Me.m_description = Description.DDATE
                        Me.dateTypeG = True
                    Else
                        ' E110 1101
                        Me.m_description = Description.DATE_TIME
                        Me.dateTypeF = True
                    End If

                Else
                    ' E110 111
                    If ((vif And 1) = 0) Then
                        ' E110 1110
                        Me.m_description = Description.HCA
                        Me.unit = DlmsUnits.DlmsUnit.RESERVED
                    Else
                        Me.m_description = Description.NOT_SUPPORTED
                    End If

                End If

            End If

        End If

    End Sub
    Private Sub decodeE10(ByVal vif As Integer)
        ' E10
        If ((vif And 16) = 0) Then
            ' E100
            If ((vif And 8) = 0) Then
                ' E100 0
                Me.m_description = Description.VOLUME_FLOW_EXT
                Me.multiplierExponent = ((vif And 7) - 7)
                Me.unit = DlmsUnits.DlmsUnit.CUBIC_METRE_PER_MINUTE
            Else
                ' E100 1
                Me.m_description = Description.VOLUME_FLOW_EXT
                Me.multiplierExponent = ((vif And 7) - 9)
                Me.unit = DlmsUnits.DlmsUnit.CUBIC_METRE_PER_SECOND
            End If

        Else
            ' E101
            If ((vif And 8) = 0) Then
                ' E101 0
                Me.m_description = Description.MASS_FLOW
                Me.multiplierExponent = ((vif And 7) - 3)
                Me.unit = DlmsUnits.DlmsUnit.KILOGRAM_PER_HOUR
            Else
                ' E101 1
                If ((vif And 4) = 0) Then
                    ' E101 10
                    Me.m_description = Description.FLOW_TEMPERATURE
                    Me.multiplierExponent = ((vif And 3) - 3)
                    Me.unit = DlmsUnits.DlmsUnit.DEGREE_CELSIUS
                Else
                    ' E101 11
                    Me.m_description = Description.RETURN_TEMPERATURE
                    Me.multiplierExponent = ((vif And 3) - 3)
                    Me.unit = DlmsUnits.DlmsUnit.DEGREE_CELSIUS
                End If

            End If

        End If

    End Sub
    Private Sub decodeE0(ByVal vif As Integer)
        ' E0
        If ((vif And 32) = 0) Then
            Me.decodeE00(vif)
        Else
            Me.decode01(vif)
        End If

    End Sub
    Private Sub decode01(ByVal vif As Integer)
        ' E01
        If ((vif And 16) = 0) Then
            ' E010
            If ((vif And 8) = 0) Then
                ' E010 0
                If ((vif And 4) = 0) Then
                    ' E010 00
                    Me.m_description = Description.ON_TIME
                Else
                    ' E010 01
                    Me.m_description = Description.OPERATING_TIME
                End If

                Me.decodeTimeUnit(vif)
            Else
                ' E010 1
                Me.m_description = Description.POWER
                Me.multiplierExponent = ((vif And 7) - 3)
                Me.unit = DlmsUnits.DlmsUnit.WATT
            End If

        Else
            ' E011
            If ((vif And 8) = 0) Then
                ' E011 0
                Me.m_description = Description.POWER
                Me.multiplierExponent = (vif And 7)
                Me.unit = DlmsUnits.DlmsUnit.JOULE_PER_HOUR
            Else
                ' E011 1
                Me.m_description = Description.VOLUME_FLOW
                Me.multiplierExponent = ((vif And 7) - 6)
                Me.unit = DlmsUnits.DlmsUnit.CUBIC_METRE_PER_HOUR
            End If

        End If

    End Sub
    Private Sub decodeE00(ByVal vif As Integer)
        ' E00
        If ((vif And 16) = 0) Then
            ' E000
            If ((vif And 8) = 0) Then
                ' E000 0
                Me.m_description = Description.ENERGY
                Me.multiplierExponent = ((vif And 7) - 3)
                Me.unit = DlmsUnits.DlmsUnit.WATT_HOUR
            Else
                ' E000 1
                Me.m_description = Description.ENERGY
                Me.multiplierExponent = (vif And 7)
                Me.unit = DlmsUnits.DlmsUnit.JOULE
            End If

        Else
            ' E001
            If ((vif And 8) = 0) Then
                ' E001 0
                Me.m_description = Description.VOLUME
                Me.multiplierExponent = ((vif And 7) - 6)
                Me.unit = DlmsUnits.DlmsUnit.CUBIC_METRE
            Else
                ' E001 1
                Me.m_description = Description.MASS
                Me.multiplierExponent = ((vif And 7) - 3)
                Me.unit = DlmsUnits.DlmsUnit.KILOGRAM
            End If

        End If

    End Sub

    ' implements table 28 of DIN EN 13757-3:2013
    Private Sub decodeMainExtendedVif(ByVal vif As Byte)
        If ((vif And 127) = 11) Then
            ' E000 1011
            Me.m_description = Description.PARAMETER_SET_ID
        ElseIf ((vif And 127) = 12) Then
            ' E000 1100
            Me.m_description = Description.MODEL_VERSION
        ElseIf ((vif And 127) = 13) Then
            ' E000 1101
            Me.m_description = Description.HARDWARE_VERSION
        ElseIf ((vif And 127) = 14) Then
            ' E000 1110
            Me.m_description = Description.FIRMWARE_VERSION
        ElseIf ((vif And 127) = 15) Then
            ' E000 1111
            Me.m_description = Description.OTHER_SOFTWARE_VERSION
        ElseIf ((vif And 127) = 16) Then
            ' E001 0000
            Me.m_description = Description.CUSTOMER_LOCATION
        ElseIf ((vif And 127) = 17) Then
            ' E001 0001
            Me.m_description = Description.CUSTOMER
        ElseIf ((vif And 127) = 18) Then
            ' E001 0010
            Me.m_description = Description.ACCESS_CODE_USER
        ElseIf ((vif And 127) = 19) Then
            ' E001 0011
            Me.m_description = Description.ACCESS_CODE_OPERATOR
        ElseIf ((vif And 127) = 20) Then
            ' E001 0100
            Me.m_description = Description.ACCESS_CODE_SYSTEM_OPERATOR
        ElseIf ((vif And 127) = 21) Then
            ' E001 0101
            Me.m_description = Description.ACCESS_CODE_SYSTEM_DEVELOPER
        ElseIf ((vif And 127) = 22) Then
            ' E001 0110
            Me.m_description = Description.PASSWORD
        ElseIf ((vif And 127) = 23) Then
            ' E001 0111
            Me.m_description = Description.ERROR_FLAGS
        ElseIf ((vif And 127) = 24) Then
            ' E001 1000
            Me.m_description = Description.ERROR_MASK
        ElseIf ((vif And 127) = 25) Then
            ' E001 1001
            Me.m_description = Description.SECURITY_KEY
        ElseIf ((vif And 127) = 26) Then
            ' E001 1010
            Me.m_description = Description.DIGITAL_OUTPUT
        ElseIf ((vif And 127) = 27) Then
            ' E001 1011
            Me.m_description = Description.DIGITAL_INPUT
        ElseIf ((vif And 127) = 28) Then
            ' E001 1100
            Me.m_description = Description.BAUDRATE
        ElseIf ((vif And 127) = 29) Then
            ' E001 1101
            Me.m_description = Description.RESPONSE_DELAY_TIME
        ElseIf ((vif And 127) = 30) Then
            ' E001 1110
            Me.m_description = Description.RETRY
        ElseIf ((vif And 127) = 31) Then
            ' E001 1111
            Me.m_description = Description.REMOTE_CONTROL
        ElseIf ((vif And 127) = 32) Then
            ' E010 0000
            Me.m_description = Description.FIRST_STORAGE_NUMBER_CYCLIC
        ElseIf ((vif And 127) = 33) Then
            ' E010 0001
            Me.m_description = Description.LAST_STORAGE_NUMBER_CYCLIC
        ElseIf ((vif And 127) = 34) Then
            ' E010 0010
            Me.m_description = Description.SIZE_STORAGE_BLOCK
        ElseIf ((vif And 127) = 35) Then
            ' E010 0011
            Me.m_description = Description.RESERVED
        ElseIf ((vif And 124) = 36) Then
            ' E010 01nn
            Me.m_description = Description.STORAGE_INTERVALL
            Me.unit = DataRecord.timeUnitFor(vif)
        ElseIf ((vif And 127) = 40) Then
            ' E010 1000
            Me.m_description = Description.STORAGE_INTERVALL
            Me.unit = DlmsUnits.DlmsUnit.MONTH
        ElseIf ((vif And 127) = 41) Then
            ' E010 1001
            Me.m_description = Description.STORAGE_INTERVALL
            Me.unit = DlmsUnits.DlmsUnit.YEAR
        ElseIf ((vif And 127) = 42) Then
            ' E010 1010
            Me.m_description = Description.OPERATOR_SPECIFIC_DATA
        ElseIf ((vif And 127) = 43) Then
            ' E010 1011
            Me.m_description = Description.TIME_POINT
            Me.unit = DlmsUnits.DlmsUnit.SECOND
        ElseIf ((vif And 124) = 44) Then
            ' E010 11nn
            Me.m_description = Description.DURATION_LAST_READOUT
            Me.unit = DataRecord.timeUnitFor(vif)
        ElseIf ((vif And 124) = 48) Then
            ' E011 00nn
            Me.m_description = Description.TARIF_DURATION
            Me.unit = DataRecord.timeUnitFor(vif)
        ElseIf ((vif And 124) = 52) Then
            ' E011 01nn
            Me.m_description = Description.TARIF_PERIOD
            Me.unit = DataRecord.timeUnitFor(vif)
        ElseIf ((vif And 127) = 56) Then
            ' E011 1000
            Me.m_description = Description.TARIF_PERIOD
            Me.unit = DlmsUnits.DlmsUnit.MONTH
        ElseIf ((vif And 127) = 57) Then
            ' E011 1001
            Me.m_description = Description.TARIF_PERIOD
            Me.unit = DlmsUnits.DlmsUnit.YEAR
        ElseIf ((vif And 112) = 64) Then
            ' E100 0000
            Me.m_description = Description.VOLTAGE
            Me.multiplierExponent = ((vif And 15) - 9)
            Me.unit = DlmsUnits.DlmsUnit.VOLT
        ElseIf ((vif And 112) = 80) Then
            ' E101 0000
            Me.m_description = Description.CURRENT
            Me.multiplierExponent = ((vif And 15) - 12)
            Me.unit = DlmsUnits.DlmsUnit.AMPERE
        ElseIf ((vif And 127) = 96) Then
            ' E110 0000
            Me.m_description = Description.RESET_COUNTER
        ElseIf ((vif And 127) = 97) Then
            ' E110 0001
            Me.m_description = Description.CUMULATION_COUNTER
        ElseIf ((vif And 127) = 98) Then
            ' E110 0010
            Me.m_description = Description.CONTROL_SIGNAL
        ElseIf ((vif And 127) = 99) Then
            ' E110 0011
            Me.m_description = Description.DAY_OF_WEEK
            ' 1 = Monday; 7 = Sunday; 0 = all Days
        ElseIf ((vif And 127) = 100) Then
            ' E110 0100
            Me.m_description = Description.WEEK_NUMBER
        ElseIf ((vif And 127) = 101) Then
            ' E110 0101
            Me.m_description = Description.TIME_POINT_DAY_CHANGE
        ElseIf ((vif And 127) = 102) Then
            ' E110 0110
            Me.m_description = Description.PARAMETER_ACTIVATION_STATE
        ElseIf ((vif And 127) = 103) Then
            ' E110 0111
            Me.m_description = Description.SPECIAL_SUPPLIER_INFORMATION
        ElseIf ((vif And 124) = 104) Then
            ' E110 10nn
            Me.m_description = Description.LAST_CUMULATION_DURATION
            Me.unit = DataRecord.unitBiggerFor(vif)
        ElseIf ((vif And 124) = 108) Then
            ' E110 11nn
            Me.m_description = Description.OPERATING_TIME_BATTERY
            Me.unit = DataRecord.unitBiggerFor(vif)
        ElseIf ((vif And 127) = 112) Then
            ' E111 0000
            Me.m_description = Description.NOT_SUPPORTED
            ' TODO: BATTERY_CHANGE_DATE_TIME
        ElseIf ((vif And 127) = 113) Then
            ' E111 0001
            Me.m_description = Description.RF_LEVEL
            Me.unit = DlmsUnits.DlmsUnit.SIGNAL_STRENGTH
        ElseIf ((vif And 127) = 114) Then
            ' E111 0010
            Me.m_description = Description.NOT_SUPPORTED
            ' TODO: DAYLIGHT_SAVING (begin, ending, deviation)
        ElseIf ((vif And 127) = 115) Then
            ' E111 0011
            Me.m_description = Description.NOT_SUPPORTED
            ' TODO: Listening window management data type L
        ElseIf ((vif And 127) = 116) Then
            ' E111 0100
            Me.m_description = Description.REMAINING_BATTERY_LIFE_TIME
            Me.unit = DlmsUnits.DlmsUnit.DAY
        ElseIf ((vif And 127) = 117) Then
            ' E111 0101
            Me.m_description = Description.NUMBER_STOPS
        ElseIf ((vif And 127) = 118) Then
            ' E111 0110
            Me.m_description = Description.MANUFACTURER_SPECIFIC
        ElseIf ((vif And 127) >= 119) Then
            ' E111 0111 - E111 1111
            Me.m_description = Description.RESERVED
        Else
            Me.m_description = Description.NOT_SUPPORTED
        End If

    End Sub
    Private Shared Function unitBiggerFor(ByVal vif As Byte) As DlmsUnits.DlmsUnit
        Dim u As Integer = (vif And 3)
        Select Case (u)
            Case 0
                ' E110 1100
                Return DlmsUnits.DlmsUnit.HOUR
            Case 1
                ' E110 1101
                Return DlmsUnits.DlmsUnit.DAY
            Case 2
                ' E110 1110
                Return DlmsUnits.DlmsUnit.MONTH
            Case 3
                ' E110 1111
                Return DlmsUnits.DlmsUnit.YEAR
            Case Else
                Throw New Exception(String.Format("Unknown unit 0x%02X.", u))
        End Select

    End Function
    Private Shared Function timeUnitFor(ByVal vif As Byte) As DlmsUnits.DlmsUnit
        Dim u As Integer = (vif And 3)
        Select Case (u)
            Case 0
                ' E010 1100
                Return DlmsUnits.DlmsUnit.SECOND
            Case 1
                ' E010 1101
                Return DlmsUnits.DlmsUnit.MIN
            Case 2
                ' E010 1110
                Return DlmsUnits.DlmsUnit.HOUR
            Case 3
                ' E010 1111
                Return DlmsUnits.DlmsUnit.DAY
            Case Else
                Throw New Exception(String.Format("Unknown unit 0x%02X.", u))
        End Select

    End Function

    ' implements table 29 of DIN EN 13757-3:2011
    Private Sub decodeAlternateExtendedVif(ByVal vif As Byte)
        Me.m_description = Description.NOT_SUPPORTED
        ' default value
        If ((vif And 64) = 0) Then
            ' E0
            If ((vif And 32) = 0) Then
                ' E00
                If ((vif And 16) = 0) Then
                    ' E000
                    If ((vif And 8) = 0) Then
                        ' E000 0
                        If ((vif And 4) = 0) Then
                            ' E000 00
                            If ((vif And 2) = 0) Then
                                ' E000 000
                                Me.m_description = Description.ENERGY
                                Me.multiplierExponent = (5 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.WATT_HOUR
                            Else
                                ' E000 001
                                Me.m_description = Description.REACTIVE_ENERGY
                                Me.multiplierExponent = (3 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.VAR_HOUR
                            End If

                        Else
                            ' E000 01
                            If ((vif And 2) = 0) Then
                                ' E000 010
                                Me.m_description = Description.APPARENT_ENERGY
                                Me.multiplierExponent = (3 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.VOLT_AMPERE_HOUR
                            Else
                                ' E000 011
                                Me.m_description = Description.NOT_SUPPORTED
                            End If

                        End If

                    Else
                        ' E000 1
                        If ((vif And 4) = 0) Then
                            ' E000 10
                            If ((vif And 2) = 0) Then
                                ' E000 100
                                Me.m_description = Description.ENERGY
                                Me.multiplierExponent = (8 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.JOULE
                            Else
                                ' E000 101
                                Me.m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E000 11
                            Me.m_description = Description.ENERGY
                            Me.multiplierExponent = (5 + (vif And 3))
                            Me.unit = DlmsUnits.DlmsUnit.CALORIFIC_VALUE
                        End If

                    End If

                Else
                    ' E001
                    If ((vif And 8) = 0) Then
                        ' E001 0
                        If ((vif And 4) = 0) Then
                            ' E001 00
                            If ((vif And 2) = 0) Then
                                ' E001 000
                                Me.m_description = Description.VOLUME
                                Me.multiplierExponent = (2 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.CUBIC_METRE
                            Else
                                ' E001 001
                                Me.m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E001 01
                            Me.m_description = Description.REACTIVE_POWER
                            Me.multiplierExponent = (vif And 3)
                            Me.unit = DlmsUnits.DlmsUnit.VAR
                        End If

                    Else
                        ' E001 1
                        If ((vif And 4) = 0) Then
                            ' E001 10
                            If ((vif And 2) = 0) Then
                                ' E001 100
                                Me.m_description = Description.MASS
                                Me.multiplierExponent = (5 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.KILOGRAM
                            Else
                                ' E001 101
                                Me.m_description = Description.REL_HUMIDITY
                                Me.multiplierExponent = (-1 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.PERCENTAGE
                            End If

                        Else
                            ' E001 11
                            Me.m_description = Description.NOT_SUPPORTED
                        End If

                    End If

                End If

            Else
                ' E01
                If ((vif And 16) = 0) Then
                    ' E010
                    If ((vif And 8) = 0) Then
                        ' E010 0
                        If ((vif And 4) = 0) Then
                            ' E010 00
                            If ((vif And 2) = 0) Then
                                ' E010 000
                                If ((vif And 1) = 0) Then
                                    ' E010 0000
                                    Me.m_description = Description.VOLUME
                                    Me.multiplierExponent = 0
                                    Me.unit = DlmsUnits.DlmsUnit.CUBIC_FEET
                                Else
                                    ' E010 0001
                                    Me.m_description = Description.VOLUME
                                    Me.multiplierExponent = -1
                                    Me.unit = DlmsUnits.DlmsUnit.CUBIC_FEET
                                End If

                            Else
                                ' E010 001
                                ' outdated value !
                                Me.m_description = Description.VOLUME
                                Me.multiplierExponent = (-1 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.US_GALLON
                            End If

                        Else
                            ' E010 01
                            If ((vif And 2) = 0) Then
                                ' E010 010
                                If ((vif And 1) = 0) Then
                                    ' E010 0100
                                    ' outdated value !
                                    Me.m_description = Description.VOLUME_FLOW
                                    Me.multiplierExponent = -3
                                    Me.unit = DlmsUnits.DlmsUnit.US_GALLON_PER_MINUTE
                                Else
                                    ' E010 0101
                                    ' outdated value !
                                    Me.m_description = Description.VOLUME_FLOW
                                    Me.multiplierExponent = 0
                                    Me.unit = DlmsUnits.DlmsUnit.US_GALLON_PER_MINUTE
                                End If

                            Else
                                ' E010 011
                                If ((vif And 1) = 0) Then
                                    ' E010 0110
                                    ' outdated value !
                                    Me.m_description = Description.VOLUME_FLOW
                                    Me.multiplierExponent = 0
                                    Me.unit = DlmsUnits.DlmsUnit.US_GALLON_PER_HOUR
                                Else
                                    ' E010 0111
                                    Me.m_description = Description.NOT_SUPPORTED
                                End If

                            End If

                        End If

                    Else
                        ' E010 1
                        If ((vif And 4) = 0) Then
                            ' E010 10
                            If ((vif And 2) = 0) Then
                                ' E010 100
                                Me.m_description = Description.POWER
                                Me.multiplierExponent = (5 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.WATT
                            Else
                                If ((vif And 1) = 0) Then
                                    ' E010 1010
                                    Me.m_description = Description.PHASE
                                    Me.multiplierExponent = -1
                                    ' is -1 or 0 correct ??
                                    Me.unit = DlmsUnits.DlmsUnit.DEGREE
                                End If

                                ' TODO same
                                ' else {
                                ' // E010 1011
                                ' description = Description.PHASE;
                                ' multiplierExponent = -1; // is -1 or 0 correct ??
                                ' unit = DlmsUnits.DlmsUnit.DEGREE;
                                ' }
                            End If

                        Else
                            ' E010 11
                            Me.m_description = Description.FREQUENCY
                            Me.multiplierExponent = (-3 + (vif And 3))
                            Me.unit = DlmsUnits.DlmsUnit.HERTZ
                        End If

                    End If

                Else
                    ' E011
                    If ((vif And 8) = 0) Then
                        ' E011 0
                        If ((vif And 4) = 0) Then
                            ' E011 00
                            If ((vif And 2) = 0) Then
                                ' E011 000
                                Me.m_description = Description.POWER
                                Me.multiplierExponent = (8 + (vif And 1))
                                Me.unit = DlmsUnits.DlmsUnit.JOULE_PER_HOUR
                            Else
                                ' E011 001
                                Me.m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E011 01
                            Me.m_description = Description.APPARENT_ENERGY
                            Me.multiplierExponent = (vif And 3)
                            Me.unit = DlmsUnits.DlmsUnit.VOLT_AMPERE
                        End If

                    Else
                        ' E011 1
                        Me.m_description = Description.NOT_SUPPORTED
                    End If

                End If

            End If

        Else
            ' E1
            If ((vif And 32) = 0) Then
                ' E10
                If ((vif And 16) = 0) Then
                    ' E100
                    Me.m_description = Description.NOT_SUPPORTED
                Else
                    ' E101
                    If ((vif And 8) = 0) Then
                        ' E101 0
                        Me.m_description = Description.NOT_SUPPORTED
                    Else
                        ' E101 1
                        If ((vif And 4) = 0) Then
                            ' E101 10
                            ' outdated value !
                            Me.m_description = Description.FLOW_TEMPERATURE
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E101 11
                            ' outdated value !
                            Me.m_description = Description.RETURN_TEMPERATURE
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_FAHRENHEIT
                        End If

                    End If

                End If

            Else
                ' E11
                If ((vif And 16) = 0) Then
                    ' E110
                    If ((vif And 8) = 0) Then
                        ' E110 0
                        If ((vif And 4) = 0) Then
                            ' E110 00
                            ' outdated value !
                            Me.m_description = Description.TEMPERATURE_DIFFERENCE
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E110 01
                            ' outdated value !
                            Me.m_description = Description.FLOW_TEMPERATURE
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_FAHRENHEIT
                        End If

                    Else
                        ' E110 1
                        Me.m_description = Description.NOT_SUPPORTED
                    End If

                Else
                    ' E111
                    If ((vif And 8) = 0) Then
                        ' E111 0
                        If ((vif And 4) = 0) Then
                            ' E111 00
                            ' outdated value !
                            Me.m_description = Description.TEMPERATURE_LIMIT
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E111 01
                            Me.m_description = Description.TEMPERATURE_LIMIT
                            Me.multiplierExponent = ((vif And 3) - 3)
                            Me.unit = DlmsUnits.DlmsUnit.DEGREE_CELSIUS
                        End If

                    Else
                        ' E111 1
                        Me.m_description = Description.MAX_POWER
                        Me.multiplierExponent = ((vif And 7) - 3)
                        Me.unit = DlmsUnits.DlmsUnit.WATT
                    End If

                End If

            End If

        End If

    End Sub
    'Public Overloads Function toString() As String
    '    Dim builder As StringBuilder = New StringBuilder
    '    builder.Append("DIB:").Append(Me.dib.ToString).Append(", VIB:").Append(Me.vib.ToString).Append(" -> descr:").Append(Me.m_description))
    '    If (Me.m_description = Description.USER_DEFINED) Then
    '        builder.Append(" :").Append(Me.getUserDefinedDescription)
    '    End If

    '    builder.Append(", function:").Append(Me.FunctionField)
    '    If (Me.storageNumber > 0) Then
    '        builder.Append(", storage:").Append(Me.storageNumber)
    '    End If

    '    If (Me.tariff > 0) Then
    '        builder.Append(", tariff:").Append(Me.tariff)
    '    End If

    '    If (Me.subunit > 0) Then
    '        builder.Append(", subunit:").Append(Me.subunit)
    '    End If

    '    Dim valuePlacHolder As String = ", value:"
    '    Dim scaledValueString As String = ", scaled value:"
    '    Select Case (Me.m_dataValueType)
    '        Case Date, String
    '            builder.Append(valuePlacHolder).Append(Me.dataValue.ToString)
    '        Case Double
    '            builder.Append(scaledValueString).Append(Me.getScaledDataValue)
    '        Case Long
    '            If (Me.multiplierExponent = 0) Then
    '                builder.Append(valuePlacHolder).Append(Me.dataValue)
    '            Else
    '                builder.Append(scaledValueString).Append(Me.getScaledDataValue)
    '            End If

    '        Case Bcd
    '            If (Me.multiplierExponent = 0) Then
    '                builder.Append(valuePlacHolder).Append(Me.dataValue.ToString)
    '            Else
    '                builder.Append(scaledValueString).Append(Me.getScaledDataValue)
    '            End If

    '        Case NONE
    '            builder.Append(", value:NONE")
    '    End Select

    '    If (Not (Me.unit) Is Nothing) Then
    '        builder.Append(", unit:").Append(Me.unit)
    '        If Not Me.unit.getUnit.isEmpty Then
    '            builder.Append(", ").Append(Me.unit.getUnit)
    '        End If

    '    End If

    '    Return builder.ToString
    'End Function
    Public Function getDataLength() As Integer
        Return Me.dataLength
    End Function
End Class

