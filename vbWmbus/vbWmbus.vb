﻿Imports System.Text
Imports Newtonsoft.Json
Imports System.Runtime.Remoting.Metadata.W3cXsd2001
Imports MaddalenaAmr

Public Class vbWmbus

    Public Enum DataValueType
        _Long
        _Double
        _Date
        _String
        BCD
        General
    End Enum
    Public Enum FunctionField
        Actual_Value
        Maximal_Value
        Minimal_Value
        Error_Value
    End Enum
    Public Enum TariffDescription
        GENERAL = 0
        COOLING_ENERGY = 1
        TARIFF2 = 2
        TARIFF3 = 3
        TARIFF4 = 4

    End Enum
    Public Enum StorageIntervalDescription
        YEARLY_STORAGE = 29
        MONTHLY_STORAGE = 28
        DAILY_STORAGE = 27
        HOURLY_STORAGE = 26
        MINUTELY_STORAGE = 25
        SECONDLY_STORAGE = 24
        GENERAL = 0
    End Enum
    Public Enum SubunitDescription
        MAIN = 0
        OBIS_B1 = 1
        OBIS_B2 = 2
        OBIS_B3 = 3
        OBIS_B4 = 4
        TARIFF_SUBUNIT = 5
        MINIMUM_SUBUNIT = 6
        MAXIMUM_SUBUNIT = 7
        DATA_LOGGER = 8
        EVENT_LOGGER = 9
        TEST_MODE = 10
        CALIBRATION_UNIT = 11
        ADJUSTMENT_UNIT = 12
        PULSE_COLLECTOR1 = 13
        PULSE_COLLECTOR2 = 14
        PULSE_COLLECTOR3 = 15
        PULSE_COLLECTOR4 = 16
        PULSE_COLLECTOR5 = 17
        PULSE_COLLECTOR6 = 18
        PULSE_COLLECTOR7 = 19
        PULSE_COLLECTOR8 = 20
        CONFIGURATION_MODE1 = 21
        CONFIGURATION_MODE2 = 22
        CONFIGURATION_MODE3 = 23
        CONFIGURATION_MODE4 = 24
        CONFIGURATION_MODE5 = 25
        CONFIGURATION_MODE6 = 26
        CONFIGURATION_MODE7 = 27
        CONFIGURATION_MODE8 = 28
        CONFIGURATION_MODE9 = 29

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
        NONE = 255
    End Enum
    Public Enum DeviceType
        OTHER = &H0
        OIL_METER = &H1
        ELECTRICITY_METER = &H2
        GAS_METER = &H3
        HEAT_METER = &H4
        STEAM_METER = &H5
        WARM_WATER_METER = &H6
        WATER_METER = &H7
        HEAT_COST_ALLOCATOR = &H8
        COMPRESSED_AIR = &H9
        COOLING_METER_OUTLET = &HA
        COOLING_METER_INLET = &HB
        HEAT_METER_INLET = &HC
        HEAT_COOLING_METER = &HD
        BUS_SYSTEM_COMPONENT = &HE
        UNKNOWN = &HF
        RESERVED_FOR_METER_16 = &H10
        RESERVED_FOR_METER_17 = &H11
        RESERVED_FOR_METER_18 = &H12
        RESERVED_FOR_METER_19 = &H13
        CALORIFIC_VALUE = &H14
        HOT_WATER_METER = &H15
        COLD_WATER_METER = &H16
        DUAL_REGISTER_WATER_METER = &H17
        PRESSURE_METER = &H18
        AD_CONVERTER = &H19
        SMOKE_DETECTOR = &H1A
        ROOM_SENSOR_TEMP_HUM = &H1B
        GAS_DETECTOR = &H1C
        RESERVED_FOR_SENSOR_H1D = &H1D
        RESERVED_FOR_SENSOR_H1E = &H1E
        RESERVED_FOR_SENSOR_H1F = &H1F
        BREAKER_ELEC = &H20
        VALVE_GAS_OR_WATER = &H21
        RESERVED_FOR_SWITCHING_DEVICE_H22 = &H22
        RESERVED_FOR_SWITCHING_DEVICE_H23 = &H23
        RESERVED_FOR_SWITCHING_DEVICE_H24 = &H24
        CUSTOMER_UNIT_DISPLAY_DEVICE = &H25
        RESERVED_FOR_CUSTOMER_UNIT_H26 = &H26
        RESERVED_FOR_CUSTOMER_UNIT_H27 = &H27
        WASTE_WATER_METER = &H28
        GARBAGE = &H29
        RESERVED_FOR_CO2 = &H2A
        RESERVED_FOR_ENV_METER_H2B = &H2B
        RESERVED_FOR_ENV_METER_H2C = &H2C
        RESERVED_FOR_ENV_METER_H2D = &H2D
        RESERVED_FOR_ENV_METER_H2E = &H2E
        RESERVED_FOR_ENV_METER_H2F = &H2F
        RESERVED_FOR_SYSTEM_DEVICES_H30 = &H30
        COM_CONTROLLER = &H31
        UNIDIRECTION_REPEATER = &H32
        BIDIRECTION_REPEATER = &H33
        RESERVED_FOR_SYSTEM_DEVICES_H34 = &H34
        RESERVED_FOR_SYSTEM_DEVICES_H35 = &H35
        RADIO_CONVERTER_SYSTEM_SIDE = &H36
        RADIO_CONVERTER_METER_SIDE = &H37
        RESERVED_FOR_SYSTEM_DEVICES_H38 = &H38
        RESERVED_FOR_SYSTEM_DEVICES_H39 = &H39
        RESERVED_FOR_SYSTEM_DEVICES_H3A = &H3A
        RESERVED_FOR_SYSTEM_DEVICES_H3B = &H3B
        RESERVED_FOR_SYSTEM_DEVICES_H3C = &H3C
        RESERVED_FOR_SYSTEM_DEVICES_H3D = &H3D
        RESERVED_FOR_SYSTEM_DEVICES_H3E = &H3E
        RESERVED_FOR_SYSTEM_DEVICES_H3F = &H3F
        RESERVED = &HFF
    End Enum
    Public Enum DlmsUnit

        YEAR = 1 ''"a")'
        MONTH = 2 ' "mo")'
        WEEK = 3 ' "wk")'
        DAY = 4 ' "d")'
        HOUR = 5 ' "h")'
        MIN = 6 ' "min")'
        SECOND = 7 ' "s")'
        DEGREE = 8 ' "°")'
        DEGREE_CELSIUS = 9 ' "°C")'
        CURRENCY = 10 ' "")'
        METRE = 11 ' "m")'
        METRE_PER_SECOND = 12 ' "m/s")'
        CUBIC_METRE = 13 ' "m³")'
        CUBIC_METRE_CORRECTED = 14 ' "m³")'
        CUBIC_METRE_PER_HOUR = 15 ' "m³/h")'
        CUBIC_METRE_PER_HOUR_CORRECTED = 16 ' "m³/h")'
        CUBIC_METRE_PER_DAY = 17 ' "m³/d")'
        CUBIC_METRE_PER_DAY_CORRECTED = 18 ' "m³/d")'
        LITRE = 19 ' "l")'
        KILOGRAM = 20 ' "kg")'
        NEWTON = 21 ' "N")'
        NEWTONMETER = 22 ' "Nm")'
        PASCAL = 23 ' "Pa")'
        BAR = 24 ' "bar")'
        JOULE = 25 ' "J")'
        JOULE_PER_HOUR = 26 ' "J/h")'
        WATT = 27 ' "W")'
        VOLT_AMPERE = 28 ' "VA")'
        VAR = 29 ' "var")'
        WATT_HOUR = 30 ' "Wh")'
        VOLT_AMPERE_HOUR = 31 ' "VAh")'
        VAR_HOUR = 32 ' "varh")'
        AMPERE = 33 ' "A")'
        COULOMB = 34 ' "C")'
        VOLT = 35 ' "V")'
        VOLT_PER_METRE = 36 ' "V/m")'
        FARAD = 37 ' "F")'
        OHM = 38 ' "Ohm")'
        OHM_METRE = 39 ' "Ohm m²/m")'
        WEBER = 40 ' "Wb")'
        TESLA = 41 ' "T")'
        AMPERE_PER_METRE = 42 ' "A/m")'
        HENRY = 43 ' "H")'
        HERTZ = 44 ' "Hz")'
        ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE = 45 ' "1/=Wh)")'
        REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE = 46 ' "1/=varh)")'
        APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE = 47 ' "1=VAh)")'
        VOLT_SQUARED_HOURS = 48 ' "V²h")'
        AMPERE_SQUARED_HOURS = 49 ' "A²h")'
        KILOGRAM_PER_SECOND = 50 ' "kg/s")'
        SIEMENS = 51 ' "S")'
        KELVIN = 52 ' "K")'
        VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE = 53 ' "1/=V²h)")'
        AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE = 54 ' "1/=A²h)")'
        METER_CONSTANT_OR_PULSE_VALUE = 55 ' "1/m³")'
        PERCENTAGE = 56 ' "%")'
        AMPERE_HOUR = 57 ' "Ah")'
        ENERGY_PER_VOLUME = 60 ' "Wh/m³")'
        CALORIFIC_VALUE = 61 ' "J/m³")'
        MOLE_PERCENT = 62 ' "Mol %")'
        MASS_DENSITY = 63 ' "g/m³")'
        PASCAL_SECOND = 64 ' "Pa s")'
        SPECIFIC_ENERGY = 65 ' "J/kg")'
        SIGNAL_STRENGTH = 70 ' "dBm")'
        SIGNAL_STRENGTH_MICROVOLT = 71 ' "dBµv")'
        LOGARITHMIC = 72 ' "dB")'
        RESERVED = 253 ' "")'
        OTHER_UNIT = 254 ' "other")'
        COUNT = 255 ' "count")'
        CUBIC_METRE_PER_SECOND = 150 ' "m³/s")'
        CUBIC_METRE_PER_MINUTE = 151 ' "m³/min")'
        KILOGRAM_PER_HOUR = 152 ' "kg/h")'
        CUBIC_FEET = 153 ' "cft")'
        US_GALLON = 154 ' "Impl. gal.")'
        US_GALLON_PER_MINUTE = 155 ' "Impl. gal./min")'
        US_GALLON_PER_HOUR = 156 ' "Impl. gal./h")'
        DEGREE_FAHRENHEIT = 157 ' "°F");
    End Enum
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
    Public Enum messageType As Integer
        SND_NR = &H44
        SND_IR = &H46
        ACC_NR = &H47
        ACC_DMD = &H48
        ACK1 = &H0
        ACK2 = &H10
        ACK3 = &H20
        ACK4 = &H30
        NACK1 = &H1
        NACK2 = &H11
        NACK3 = &H21
        NACK4 = &H33
        RSP_UD1 = &H8
        RSP_UD2 = &H18
        RSP_UD3 = &H28
        RSP_UD4 = &H38
    End Enum

#Region "Dichiarazioni"
    Private m_functionField As FunctionField
    Private m_description As Description
    Private userDefinedDescription As String
    Private unit As DlmsUnit
    Private multiplierExponent As Integer = 0
    Private dateTypeF As Boolean = False
    Private dateTypeG As Boolean = False
    ' // Data Information Block that contains a DIF and optionally up to 10 DIFEs
    Private dib() As Byte
    ' // Value Information Block that contains a VIF and optionally up to 10 VIFEs
    Private vib() As Byte
    Private dataValue As Object
    Private m_dataValueType As DataValueType
    ' max is 41 bits
    Private tariff As Integer
    ' max 20 bits
    Private subunit As Long
    Private dataLength As Integer
    Private storageNumber As Long
    Dim DecodedData As New DecodedDeviceClass
    Dim calendar As DateTime
    Private manufacturerId As String
    Private manufacturerDetails As String
    Private deviceId As String
    Private version As Integer
    Private deviceTypeID As String
    Private accessNumber As String
    Private status As String
    Private signature As String
    Private messageTypeID As String

    Public Const OFFSET_BYTE1 = 0
    Public Const OFFSET_BYTE2 = 2
    Public Const OFFSET_BYTE3 = 4
    Public Const OFFSET_BYTE4 = 6
    Public Const FRAME_A_SHORT = "01"
    Public Const FRAME_A_LONG = "01"
    Public Const FRAME_B = "02"
    Public Const FRAME_RC = "03"

#End Region
    ''' <summary>
    ''' Routine for decoding the WMBUS
    ''' </summary>
    ''' <param name="telegramma">Telegram to be decoded. </param>
    ''' <returns>Return a tuple.</returns>
    ''' <remarks></remarks>
    Public Function MainVBWMBUS(ByVal telegramma As String) As Tuple(Of DecodedDeviceClass, String)

        '                           0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9
        'Dim telegramma As String = "2644C5148003602400077A912B00202F2F046D252A562B0413DF00000001FD17400478A1DBB90000"
        'Dim telegramma As String = "2844C5148910504301167289105043C5140016D22B00202F2F046D19295C2B0413E800000001FD17580097FC"
        'Dim telegramma As String = "5244C51445195824010D7A4F2000202F2F046D2B275C2B0406000000008410060000000001FD1700041501000000043B00000000042B00000000025F140004610300000084401410000000848040140000000000"
        'Dim telegramma As String = "2844C5149310504301167210010000C51400162B2B00202F2F046D18295C2B0413F900000001FD1758005000"
        'Dim telegramma As String = "2B44C5144735902455087247359024C5145508560000002F2F0B6E000000426C3F2C4B6E00000002FD17000000DF21C6"
        'Dim telegramma As String = "E344E3102290980601087AD30A00000F01F9162B450040000000000000A10100D700000000000000000000000000000000000000000000002E00004600006500006F00004A00000F00000000000000000000000000001200000400002A00005A00006A0000590000420000D9000701D000FC0014012B015C01430143013D01300157023D025E02740249023D022D015E014C01520136021E02350245025C025A02450208011C014801310132012D0120013E013E0175017201570102011F014B013C0142012A01020111014D014C012C012801A5413D2129AB4A214D70D6F9CC128005C900000000000000"
        'Dim telegramma As String = "5044243434398583000D7A9B0000202F2F046D01295D2B0406000000008410060000000001FD1710041303000000043B00000000042B00000000025B1700025F17000261080003FD0C05000002FD0B301100F285"

        'E310 = digicom
        '2434 = Maddalena
        'C514 = Engelmann

        DecodedData = New DecodedDeviceClass

        Dim i As Integer = 0
        Dim buffer As Byte() = {}
        Dim j As Integer = 0

        'Preamble decoding
        For j = 0 To telegramma.Length - 1 Step 2
            Array.Resize(buffer, buffer.Length + 1)
            buffer(j / 2) = Convert.ToInt16(Strings.Mid(telegramma, j + 1, 2), 16)
        Next
        decodePreamble(buffer)

        'search for the AES marker (if not found = encrypted)
        Dim found1 As Boolean = InStr(telegramma.ToUpper, "2F2F")
        Dim frame_type As String = telegramma.Substring(32, 2)


        'Decode the encrypted frame
        If Not found1 And manufacturerId = "MAD" Then
            telegramma = DecodeAES(telegramma)
            If telegramma = "" Then Return nothing
        ElseIf Not found1 And Not manufacturerId = "MAD" Then
            MsgBox("Device produced by " & manufacturerDetails & ". Encrypted telegram, unknown AES key")
            Return Nothing
        End If
        If Not found1 And (frame_type = FRAME_A_SHORT Or frame_type = FRAME_B) Then
            MsgBox("Presumably not a Wmbus telegram. DLMS/COSEM?")
            Return Nothing
        End If


        'If it was encrypted, recreate the buffer this time decrypted
        For j = 0 To telegramma.Length - 1 Step 2
            Array.Resize(buffer, buffer.Length + 1)
            buffer(j / 2) = Convert.ToInt16(Strings.Mid(telegramma, j + 1, 2), 16)
        Next

        'Unpack all the packaged packages
        Dim bufferSpacchettato = spacchetta(buffer)

        'Reassign buffer one package at a time
        ReDim buffer(0)

        For n = 0 To bufferSpacchettato.Length - 1
            Array.Resize(buffer, bufferSpacchettato(n).Length)
            buffer = bufferSpacchettato(n)

            decodeDib(buffer, 0)
            Dim dataField As Integer = buffer(0) And &B1111
            'If dataField = 11 Or dataField = 66 Then dataField = 3
            dataLength = dataField
            If buffer(0) > 127 Then
                storageNumber = (buffer(1) And &B1111) + ((buffer(0) And &B1000000) >> 6)
                dib = {buffer(0), buffer(1)}
            Else
                storageNumber = (buffer(0) And &B1000000) >> 6
                dib = {buffer(0)}
            End If

            subunit = 0
            tariff = 0
            Dim numDife As Integer = 0

            If buffer.Length > 2 Then
                If buffer(1) = &HFD And buffer(2) = &H23 Then
                    subunit = subunit + (((buffer(3) And &H40) >> 6) << numDife)
                    tariff = tariff + ((buffer(3) And &H30) >> 4) << (numDife * 2)
                    storageNumber = storageNumber + ((buffer(3) And 15) << ((numDife * 4) + 1))
                    numDife = (numDife + 1)
                End If
            End If

            unit = 0
            multiplierExponent = 0


            Dim vif As Integer = buffer(dib.Length)
            Dim decodeFurtherVifs As Boolean = False

            If (vif = 251) Then
                decodeAlternateExtendedVif(buffer(1))
                If ((buffer(1) And 128) = 128) Then
                    decodeFurtherVifs = True
                End If
            ElseIf (vif = 253) Then
                decodeMainExtendedVif(buffer(1))
                If ((buffer(1) And 128) = 128) Then
                    decodeFurtherVifs = True
                End If
            Else
                decodeMainVif(vif)
                If ((vif And 128) = 128) Then
                    decodeFurtherVifs = True
                End If
            End If
            If Not decodeFurtherVifs Then
                vib = {buffer(dib.Length)}
            Else
                vib = {buffer(dib.Length), buffer(dib.Length + 1)}
            End If

            Select Case (dataField)
                Case 0, 8
                    dataValue = Nothing
                    m_dataValueType = DataValueType.General
                Case 1
                    dataValue = CLng(buffer(dib.Length + vib.Length))
                    m_dataValueType = DataValueType._Long

                Case 2
                    If dateTypeG Then
                        Dim day As Integer = (buffer(vib.Length + dib.Length) And 31)
                        Dim dayEffective As String = ""
                        If day = 0 Then
                            dayEffective = "Every day. "
                        End If

                        Dim month As Integer = (buffer(vib.Length + dib.Length + 1) And 15)
                        Dim monthEffective As String = ""
                        If month = 15 Then
                            monthEffective = "Every month. "
                        End If

                        Dim year1 As Integer = (buffer(vib.Length + dib.Length) And 224) >> 5
                        Dim year2 As Integer = (buffer(vib.Length + dib.Length + 1) And 240) >> 1
                        Dim year As Integer = 2000 + year1 + year2

                        Dim calendar As DateTime
                        If dayEffective = "" And monthEffective = "" Then
                            calendar = New DateTime(year, month, day, 0, 0, 0)
                            dataValue = calendar.Date.ToShortDateString
                            m_dataValueType = DataValueType._Date
                        Else
                            calendar = Nothing
                            dataValue = dayEffective + monthEffective
                        End If


                    Else
                        dataValue = CLng(buffer(vib.Length + dib.Length) Or buffer(vib.Length + dib.Length + 1) * 256)
                        If dataValue > 32768 Then
                            dataValue = dataValue - 65535
                        End If
                        m_dataValueType = DataValueType._Long
                    End If

                Case 3
                    If ((buffer((vib.Length + dib.Length + 2)) And 128) = 128) Then
                        ' negative
                        dataValue = CLng(buffer(vib.Length + dib.Length) Or
                        (buffer(vib.Length + dib.Length + 1) * 256) Or
                        ((buffer(vib.Length + dib.Length + 2) * 65536) Or 255) << 24)
                    Else
                        dataValue = CLng(buffer(vib.Length + dib.Length) Or
                        (buffer(vib.Length + dib.Length + 1) << 8) Or
                        (buffer(vib.Length + dib.Length + 2) << 256))
                    End If

                    m_dataValueType = DataValueType._Long
                Case 4
                    If dateTypeF Then
                        Dim min As Integer = (buffer(vib.Length + dib.Length) And 63)
                        Dim hour As Integer = (buffer(vib.Length + dib.Length + 1) And 31)
                        Dim day As Integer = (buffer(vib.Length + dib.Length + 2) And 31)
                        Dim mon As Integer = (buffer(vib.Length + dib.Length + 3) And 15)

                        Dim yearh As Integer = (buffer(vib.Length + dib.Length + 1) And 96) >> 5
                        Dim year1 As Integer = (buffer(vib.Length + dib.Length + 2) And 224) >> 5
                        Dim year2 As Integer = (buffer(vib.Length + dib.Length + 3) And 240) >> 1

                        Dim year As Integer
                        If yearh = 0 Or mon = 0 Or day = 0 Or hour > 23 Then
                            MsgBox("Invalid date. Reverting to default.")
                            calendar = New DateTime(1900, 1, 1, 0, 0, 0)
                        Else
                            year = 1900 + 100 * yearh + year1 + year2
                            calendar = New DateTime(year, mon, day, hour, min, 0)
                        End If

                        dataValue = calendar.TimeOfDay
                        m_dataValueType = DataValueType._Date

                    Else
                        dataValue = CLng(buffer(vib.Length + dib.Length) Or
                        (CLng(buffer(vib.Length + dib.Length + 1)) << 8) Or
                        (CLng(buffer(vib.Length + dib.Length + 2)) << 16) Or (CLng(buffer(vib.Length + dib.Length + 3)) << 24))
                        m_dataValueType = DataValueType._Long
                    End If

                Case 5
                    Dim bytebuffer As Byte() = CopyofRange(buffer, vib.Length + dib.Length, vib.Length + dib.Length + 4)
                    Dim doubleDatavalue As Double = BitConverter.ToSingle(bytebuffer, 0)
                    dataValue = doubleDatavalue
                    m_dataValueType = DataValueType._Double
                Case 6
                    If (buffer(vib.Length + dib.Length + 5) And 128) = 128 Then
                        ' negative
                        dataValue = buffer(vib.Length + dib.Length) Or
                        buffer(vib.Length + dib.Length + 1) << 8 Or
                        buffer(vib.Length + dib.Length + 2) << 16 Or
                        buffer(vib.Length + dib.Length + 3) << 24 Or
                        CType(buffer(vib.Length + dib.Length + 4), Long) << 32 Or
                        CType(buffer(vib.Length + dib.Length + 5), Long) << 40 Or (255 << 48) Or (256 << 56)
                    Else
                        dataValue = CLng((buffer(vib.Length + dib.Length) Or
                          buffer(vib.Length + dib.Length + 1) << 8 Or
                          buffer(vib.Length + dib.Length + 2) << 16 Or
                          buffer(vib.Length + dib.Length + 3) << 24 Or
                          CType(buffer(vib.Length + dib.Length + 4), Long) << 32) Or
                          CType(buffer(vib.Length + dib.Length + 5), Long) << 40)
                    End If
                    m_dataValueType = DataValueType._Long
                Case 7
                    dataValue = CLng(buffer(vib.Length + dib.Length) Or
                                 buffer(vib.Length + dib.Length + 1) << 8 Or
                                 buffer(vib.Length + dib.Length + 2) << 16 Or
                                 buffer(vib.Length + dib.Length + 3) << 24 Or
                                 CType(buffer(vib.Length + dib.Length + 4), Long) << 32 Or
                                 CType(buffer(vib.Length + dib.Length + 5), Long) << 40 Or
                                 CType(buffer(vib.Length + dib.Length + 6), Long) << 48 Or
                                 CType(buffer(vib.Length + dib.Length + 7), Long) << 56)
                    m_dataValueType = DataValueType._Long
                Case 9
                    i = setBCD(buffer, vib.Length + dib.Length, 1)
                Case 10
                    i = setBCD(buffer, vib.Length + dib.Length, 2)
                Case 11
                    i = setBCD(buffer, vib.Length + dib.Length, 3)
                Case 12
                    i = setBCD(buffer, vib.Length + dib.Length, 4)
                Case 14
                    i = setBCD(buffer, vib.Length + dib.Length, 6)
                Case 13
                    'Exit Select
                    Dim variableLength As Integer = buffer(vib.Length + dib.Length + 1)
                    Dim dataLength0x0d As Integer
                    If variableLength < 192 Then
                        dataLength0x0d = variableLength
                    ElseIf variableLength >= 192 AndAlso variableLength <= 201 Then
                        dataLength0x0d = 2 * (variableLength - 192)
                    ElseIf variableLength >= 208 AndAlso variableLength <= 217 Then
                        dataLength0x0d = 2 * (variableLength - 208)
                    ElseIf variableLength >= 224 AndAlso variableLength <= 239 Then
                        dataLength0x0d = variableLength - 224
                    ElseIf variableLength = 248 Then
                        dataLength0x0d = 4
                    Else
                        Throw New Exception("Unsupported LVAR Field: " + variableLength)
                    End If

                    Dim rawData() As Byte = New Byte(dataLength0x0d - 1) {}
                    For k = 0 To dataLength0x0d - 1
                        rawData(k) = buffer(vib.Length + dib.Length + dataLength0x0d - 1 - k)
                        k = (k + 1)
                    Next

                    dataValue = System.Text.Encoding.ASCII.GetString(rawData)
                    m_dataValueType = DataValueType._String
                Case Else
                    Dim msg As String = String.Format("Unknown Data Field in DIF: " & dataField.ToString)
                    Throw New Exception(msg)
            End Select

            If dataValue Is Nothing Then
                dataValue = 0
            End If
            Dim DataX As String = ""
            If IsNumeric(dataValue) Then
                DataX = Format(dataValue * 10 ^ multiplierExponent, "###0.##")
            ElseIf TypeOf (dataValue) Is Byte() Then
                If dataValue.length = 1 Then
                        Dim temp As Integer
                        Integer.TryParse(Hex(dataValue(0)), temp)
                        DataX = Format(temp * 10 ^ multiplierExponent, "###0.##")
                    ElseIf dataValue.length = 2 Then
                        Dim temp As Integer
                        Integer.TryParse(Hex(dataValue(1)) & Hex(dataValue(0)), temp)
                        DataX = Format(temp * 10 ^ multiplierExponent, "###0.##")
                    ElseIf dataValue.length = 3 Then
                        Dim temp As Integer
                        Integer.TryParse(Hex(dataValue(2)) & Hex(dataValue(1)) & Hex(dataValue(0)), temp)
                        DataX = Format(temp * 10 ^ multiplierExponent, "###0.##")
                    ElseIf dataValue.length = 4 Then
                        Dim temp As Integer
                        Integer.TryParse(Hex(dataValue(3)) & Hex(dataValue(2)) & Hex(dataValue(1)) & Hex(dataValue(0)), temp)
                        DataX = Format(temp * 10 ^ multiplierExponent, "###0.##")
                    ElseIf dataValue.length = 6 Then
                        Dim temp As Integer
                        Integer.TryParse(Hex(dataValue(5)) & Hex(dataValue(4)) & Hex(dataValue(3)) & Hex(dataValue(2)) & Hex(dataValue(1)) & Hex(dataValue(0)), temp)
                        DataX = Format(temp * 10 ^ multiplierExponent, "###0.##")

                    End If
                Else
                    DataX = dataValue.ToString
            End If
            If TypeOf (dataValue) Is TimeSpan Then
                DataX = calendar.ToString
            End If

            dateTypeF = False
            dateTypeG = False


            Array.Resize(DecodedData.DecodedDataField, DecodedData.DecodedDataField.Length + 1)
            DecodedData.manufacturerId = manufacturerId
            DecodedData.manufacturerDetails = manufacturerDetails
            DecodedData.deviceId = deviceId
            DecodedData.version = version
            DecodedData.deviceType = deviceTypeID
            DecodedData.accessNumber = accessNumber
            DecodedData.status = status
            DecodedData.signature = signature
            DecodedData.messageTypeID = messageTypeID


            If calendar.Date <> #01/01/0001# Then
                dataValue = calendar
            End If

            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).datavalue = dataValue
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).datavalueSimplified = DataX
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).m_datavaluetype = [Enum].GetName(GetType(DataValueType), m_dataValueType)
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).m_description = [Enum].GetName(GetType(Description), m_description)
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).m_functionField = [Enum].GetName(GetType(FunctionField), m_functionField)


            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).multiplierexponent = multiplierExponent
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).storagenumber = storageNumber


            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).subunit = [Enum].GetName(GetType(SubunitDescription), subunit)
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).tariff = [Enum].GetName(GetType(TariffDescription), tariff)
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).unitDescription = [Enum].GetName(GetType(DlmsUnit), unit)
            DecodedData.DecodedDataField(DecodedData.DecodedDataField.Length - 1).unitSymbol = getUnit(unit)

            calendar = Nothing
            dataValue = Nothing
            multiplierExponent = 0
            m_dataValueType = 0
            m_description = 0
            m_functionField = 0
            storageNumber = 0
            subunit = 0
            tariff = 0
            unit = 0

        Next

        Dim json As String = JsonConvert.SerializeObject(DecodedData)

        Return New Tuple(Of DecodedDeviceClass, String)(DecodedData, json)


    End Function
    Private Function spacchetta(ByVal buffer As Byte()) As Byte()()
        Dim bufferSpacchettato As Byte()() = {}
        Dim payloadStart As Integer = 0
        For k = 0 To buffer.Length - 2
            If buffer(k) = &H2F And buffer(k + 1) = &H2F Then
                payloadStart = k + 2
                Exit For
            End If
        Next
        If payloadStart = 0 Then
            payloadStart = 9
        End If

        Dim payload As Byte() = {}
        Array.Resize(payload, buffer.Length - payloadStart)
        Array.Copy(buffer, payloadStart, payload, 0, buffer.Length - payloadStart)

        For j = 0 To payload.Length - 2
            Dim DIF = payload(j) And &B1111
            If Not DIF = 0 Then

                Dim existDIFE As Boolean = False
                Dim existVIFE As Boolean = False
                Dim existDIFE2 As Boolean = False
                Dim existVIFE2 As Boolean = False
                Dim existDIFE3 As Boolean = False
                Dim existVIFE3 As Boolean = False
                Dim existDIFE4 As Boolean = False
                Dim existVIFE4 As Boolean = False
                Dim existDIFE5 As Boolean = False
                Dim existVIFE5 As Boolean = False
                Dim existDIFE6 As Boolean = False
                Dim existVIFE6 As Boolean = False
                Dim existDIFE7 As Boolean = False
                Dim existVIFE7 As Boolean = False

                If payload(j) > 127 Then
                    existDIFE = True
                    If payload(j + 1) > 127 Then
                        existDIFE2 = True
                        If payload(j + 2) > 127 Then
                            existDIFE3 = True
                            If payload(j + 3) > 127 Then
                                existDIFE4 = True
                                If payload(j + 4) > 127 Then
                                    existDIFE5 = True
                                    If payload(j + 5) > 127 Then
                                        existDIFE6 = True
                                        If payload(j + 6) > 127 Then
                                            existDIFE7 = True
                                        End If
                                    End If
                                End If
                            End If
                        End If
                    End If
                End If

                Dim DLMS As Boolean = False
                Dim datafieldLength As Integer = 0
                Select Case DIF
                    Case 0 To 6
                        datafieldLength = DIF
                    Case 7
                        datafieldLength = 8
                    Case 8
                        If payload(j + 1) = &H7A Then 'selection for readout
                            DLMS = True
                            datafieldLength = 4
                        End If
                    Case 9
                        datafieldLength = 1
                    Case 10
                        datafieldLength = 2
                    Case 11
                        datafieldLength = 3
                    Case 12
                        datafieldLength = 4
                    Case 13
                        datafieldLength = 0'tbd
                    Case 14
                        datafieldLength = 6
                    Case 15
                        datafieldLength = 0 'tbd
                End Select

                'The pointer must skip all DIFE (only 2 implemented to date)
                If payload(j + 1 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                    existVIFE = True
                    If payload(j + 2 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                        existVIFE2 = True
                        If payload(j + 3 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                            existVIFE3 = True
                            If payload(j + 4 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                                existVIFE4 = True
                                If payload(j + 5 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                                    existVIFE5 = True
                                    If payload(j + 6 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                                        existVIFE6 = True
                                        If payload(j + 7 + 1 * (-existDIFE) + 1 * (-existDIFE2) + 1 * (-existDIFE3) + 1 * (-existDIFE4) + 1 * (-existDIFE5) + 1 * (-existDIFE6) + 1 * (-existDIFE7)) > 127 Then
                                            existVIFE7 = True
                                        End If
                                    End If
                                End If
                            End If
                        End If
                    End If
                End If

                Dim dataBlock As Byte() = {}
                Dim lunghezzaDaCopiare = datafieldLength + 2 'which are the two mandatory for DIF and for VIF
                If existDIFE Then lunghezzaDaCopiare += 1
                If existVIFE Then lunghezzaDaCopiare += 1
                If existDIFE2 Then lunghezzaDaCopiare += 1
                If existVIFE2 Then lunghezzaDaCopiare += 1
                If existDIFE3 Then lunghezzaDaCopiare += 1
                If existVIFE3 Then lunghezzaDaCopiare += 1
                If existDIFE4 Then lunghezzaDaCopiare += 1
                If existVIFE4 Then lunghezzaDaCopiare += 1
                If existDIFE5 Then lunghezzaDaCopiare += 1
                If existVIFE5 Then lunghezzaDaCopiare += 1
                If existDIFE6 Then lunghezzaDaCopiare += 1
                If existVIFE6 Then lunghezzaDaCopiare += 1
                If existDIFE7 Then lunghezzaDaCopiare += 1
                If existVIFE7 Then lunghezzaDaCopiare += 1

                Array.Resize(dataBlock, lunghezzaDaCopiare)
                If j + payloadStart + lunghezzaDaCopiare > buffer.Length Then
                    Exit For
                End If
                Array.Copy(buffer, j + payloadStart, dataBlock, 0, lunghezzaDaCopiare)

                If dataBlock.SequenceEqual({47, 47}) Then
                    Return bufferSpacchettato
                End If

                Array.Resize(bufferSpacchettato, bufferSpacchettato.Length + 1)
                bufferSpacchettato(bufferSpacchettato.Length - 1) = dataBlock
                j = j + lunghezzaDaCopiare - 1

            End If
        Next


        Return bufferSpacchettato
    End Function
    Private Sub decodePreamble(ByVal buffer As Byte())

        Dim bais As Byte() = CopyofRange(buffer, 0, 14)
        'Me.hashCode = Arrays.hashCode(Me.bytes)
        Try
            messageTypeID = getMessageType(bais(1))
            deviceId = decodeDeviceId(bais)
            manufacturerId = decodeManufacturerId(bais)
            manufacturerDetails = selectManufacturerDetailsClass.selectManufacturerDetails(manufacturerId)
            version = bais(8)
            deviceTypeID = [Enum].GetName(GetType(DeviceType), bais(9))
            accessNumber = bais(10)
            status = decodeStatusType(bais(11))
            signature = Hex(bais(12)).ToString.PadLeft(2, "0") & Hex(bais(13)).ToString.PadLeft(2, "0")

        Catch e As Exception
            ' should not occur
            Throw New Exception(e.Message)
        End Try
    End Sub
    Private Function decodeStatusType(ByVal bais As Byte) As String
        Dim idArray As String = ""

        If bais = 0 Then
            idArray = idArray & "No Error. "
        ElseIf bais And 1 = 1 Then
            idArray = idArray & "Application busy. "
        ElseIf bais = 2 Then
            idArray = idArray & "Error present. "
        ElseIf bais = 3 Then
            idArray = idArray & "Error present. "
        End If
        If (bais And &B100) >> 2 = 1 Then
            idArray = idArray & "Power low. "
        End If
        If (bais And &B1000) >> 3 = 1 Then
            idArray = idArray & "Permanent error. "
        End If
        If (bais And &B10000) >> 4 = 1 Then
            idArray = idArray & "Temporary error. "
        End If
        If (bais And &B100000) >> 5 = 1 Then
            idArray = idArray & "Other manufacturer specific error. "
        End If
        If (bais And &B1000000) >> 6 = 1 Then
            idArray = idArray & "Other manufacturer specific error. "
        End If
        If (bais And &B10000000) >> 7 = 1 Then
            idArray = idArray & "Other manufacturer specific error. "
        End If

        Return idArray
    End Function
    Private Function decodeDeviceId(ByVal bais As Byte()) As String
        Dim idArray As String = Hex(bais(7)).ToString.PadLeft(2, "0") & Hex(bais(6)).ToString.PadLeft(2, "0") &
            Hex(bais(5)).ToString.PadLeft(2, "0") & Hex(bais(4)).ToString.PadLeft(2, "0")
        Return idArray
    End Function
    Private Function decodeManufacturerId(ByVal bais As Byte()) As String
        Dim manufacturerIdAsInt As Integer = ((bais(2) And 255) + (bais(3) * 256))
        Dim c As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        manufacturerIdAsInt = (manufacturerIdAsInt \ 2 ^ 5)
        Dim c1 As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        manufacturerIdAsInt = (manufacturerIdAsInt \ 2 ^ 5)
        Dim c2 As Char = ChrW((manufacturerIdAsInt And 31) + 64)
        Dim sb As New StringBuilder
        Return sb.Append(c2).Append(c1).Append(c).ToString
    End Function
    Private Function decodeUserDefinedVif(ByVal buffer() As Byte, ByVal offset As Integer) As Integer
        Dim length As Integer = buffer(offset)
        Dim sb As StringBuilder = New StringBuilder
        Dim i As Integer = (dib.Length + vib.Length)
        'Do While (i > offset)
        '    sb.Append(buffer(i).ToString)
        '    i = (i - 1)
        'Loop
        'Routine da scrivere
        m_description = Description.USER_DEFINED
        'userDefinedDescription = sb.ToString
        Return 0 '(length + 1)
    End Function
    Private Sub decodeMainVif(ByVal vif As Integer)
        m_description = Description.NOT_SUPPORTED
        If ((vif And 64) = 0) Then
            decodeE0(vif)
        Else
            decodeE1(vif)
        End If

    End Sub
    Private Sub decodeMainExtendedVif(ByVal vif As Byte)
        Select Case (vif And 127)
            Case 11
                ' E000 1011
                m_description = Description.PARAMETER_SET_ID
            Case 12
                ' E000 1100
                m_description = Description.MODEL_VERSION
                Case 13
                ' E000 1101
                m_description = Description.HARDWARE_VERSION
                Case 14
                ' E000 1110
                m_description = Description.FIRMWARE_VERSION
                Case 15
                ' E000 1111
                m_description = Description.OTHER_SOFTWARE_VERSION
                Case 16
                ' E001 0000
                m_description = Description.CUSTOMER_LOCATION
                Case 17
                ' E001 0001
                m_description = Description.CUSTOMER
                Case 18
                ' E001 0010
                m_description = Description.ACCESS_CODE_USER
                Case 19
                ' E001 0011
                m_description = Description.ACCESS_CODE_OPERATOR
                Case 20
                ' E001 0100
                m_description = Description.ACCESS_CODE_SYSTEM_OPERATOR
                Case 21
                ' E001 0101
                m_description = Description.ACCESS_CODE_SYSTEM_DEVELOPER
                Case 22
                ' E001 0110
                m_description = Description.PASSWORD
                Case 23
                ' E001 0111
                m_description = Description.ERROR_FLAGS
                Case 24
                ' E001 1000
                m_description = Description.ERROR_MASK
                Case 25
                ' E001 1001
                m_description = Description.SECURITY_KEY
                Case 26
                ' E001 1010
                m_description = Description.DIGITAL_OUTPUT
                Case 27
                ' E001 1011
                m_description = Description.DIGITAL_INPUT
                Case 28
                ' E001 1100
                m_description = Description.BAUDRATE
                Case 29
                ' E001 1101
                m_description = Description.RESPONSE_DELAY_TIME
                Case 30
                ' E001 1110
                m_description = Description.RETRY
                Case 31
                ' E001 1111
                m_description = Description.REMOTE_CONTROL
                Case 32
                ' E010 0000
                m_description = Description.FIRST_STORAGE_NUMBER_CYCLIC
                Case 33
                ' E010 0001
                m_description = Description.LAST_STORAGE_NUMBER_CYCLIC
                Case 34
                ' E010 0010
                m_description = Description.SIZE_STORAGE_BLOCK
                Case 35
                ' E010 0011
                m_description = Description.RESERVED
            Case 40
                ' E010 1000
                m_description = Description.STORAGE_INTERVALL
                unit = DlmsUnit.MONTH
                Case 41
                ' E010 1001
                m_description = Description.STORAGE_INTERVALL
                unit = DlmsUnit.YEAR
                Case 42
                ' E010 1010
                m_description = Description.OPERATOR_SPECIFIC_DATA
                Case 43
                ' E010 1011
                m_description = Description.TIME_POINT
                unit = DlmsUnit.SECOND
            Case 56
                ' E011 1000
                m_description = Description.TARIF_PERIOD
                unit = DlmsUnit.MONTH
                Case 57
                ' E011 1001
                m_description = Description.TARIF_PERIOD
                unit = DlmsUnit.YEAR
            Case 96
                ' E110 0000
                m_description = Description.RESET_COUNTER
            Case 97
                ' E110 0001
                m_description = Description.CUMULATION_COUNTER
            Case 98
                ' E110 0010
                m_description = Description.CONTROL_SIGNAL
            Case 99
                ' E110 0011
                m_description = Description.DAY_OF_WEEK
                ' 1 = Monday; 7 = Sunday; 0 = all Days
            Case 100
                ' E110 0100
                m_description = Description.WEEK_NUMBER
            Case 101
                ' E110 0101
                m_description = Description.TIME_POINT_DAY_CHANGE
            Case 102
                ' E110 0110
                m_description = Description.PARAMETER_ACTIVATION_STATE
            Case 103
                ' E110 0111
                m_description = Description.SPECIAL_SUPPLIER_INFORMATION
            Case 112
                ' E111 0000
                m_description = Description.NOT_SUPPORTED
                ' TODO: BATTERY_CHANGE_DATE_TIME
            Case 113
                ' E111 0001
                m_description = Description.RF_LEVEL
                unit = DlmsUnit.SIGNAL_STRENGTH
            Case 114
                ' E111 0010
                m_description = Description.NOT_SUPPORTED
                ' TODO: DAYLIGHT_SAVING (begin, ending, deviation)
            Case 115
                ' E111 0011
                m_description = Description.NOT_SUPPORTED
                ' TODO: Listening window management data type L
            Case 116
                ' E111 0100
                m_description = Description.REMAINING_BATTERY_LIFE_TIME
                unit = DlmsUnit.DAY
            Case 117
                ' E111 0101
                m_description = Description.NUMBER_STOPS
            Case 118
                ' E111 0110
                m_description = Description.MANUFACTURER_SPECIFIC
                'Case 119 To 127
                '    ' E111 0111 - E111 1111
                '    m_description = Description.RESERVED
            Case Else
                If (vif And 124) = 104 Then
                    ' E110 10nn
                    m_description = Description.LAST_CUMULATION_DURATION
                    unit = unitBiggerFor(vif)
                ElseIf (vif And 124) = 124 Then
                    ' Trick to decode the 01FD17 telegrams of Engelmann Maddalena
                    m_description = Description.ERROR_MASK
                ElseIf (vif And 124) = 108 Then
                    ' E110 11nn
                    m_description = Description.OPERATING_TIME_BATTERY
                    unit = unitBiggerFor(vif)
                ElseIf (vif And 112) = 64 Then
                    ' E100 0000
                    m_description = Description.VOLTAGE
                    multiplierExponent = ((vif And 15) - 9)
                    unit = DlmsUnit.VOLT
                ElseIf (vif And 112) = 80 Then
                    ' E101 0000
                    m_description = Description.CURRENT
                    multiplierExponent = ((vif And 15) - 12)
                    unit = DlmsUnit.AMPERE
                ElseIf (vif And 124) = 44 Then
                    ' E010 11nn
                    m_description = Description.DURATION_LAST_READOUT
                    unit = timeUnitFor(vif)
                ElseIf (vif And 124) = 48 Then
                    ' E011 00nn
                    m_description = Description.TARIF_DURATION
                    unit = timeUnitFor(vif)
                ElseIf (vif And 124) = 52 Then
                    ' E011 01nn
                    m_description = Description.TARIF_PERIOD
                    unit = timeUnitFor(vif)
                ElseIf (vif And 124) = 36 Then
                    ' E010 01nn
                    m_description = Description.STORAGE_INTERVALL
                    unit = timeUnitFor(vif)
                Else
                    m_description = Description.NONE
                End If

        End Select




    End Sub
    Private Sub decodeAlternateExtendedVif(ByVal vif As Byte)
        m_description = Description.NOT_SUPPORTED
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
                                m_description = Description.ENERGY
                                multiplierExponent = (5 + (vif And 1))
                                unit = DlmsUnit.WATT_HOUR
                            Else
                                ' E000 001
                                m_description = Description.REACTIVE_ENERGY
                                multiplierExponent = (3 + (vif And 1))
                                unit = DlmsUnit.VAR_HOUR
                            End If

                        Else
                            ' E000 01
                            If ((vif And 2) = 0) Then
                                ' E000 010
                                m_description = Description.APPARENT_ENERGY
                                multiplierExponent = (3 + (vif And 1))
                                unit = DlmsUnit.VOLT_AMPERE_HOUR
                            Else
                                ' E000 011
                                m_description = Description.NOT_SUPPORTED
                            End If

                        End If

                    Else
                        ' E000 1
                        If ((vif And 4) = 0) Then
                            ' E000 10
                            If ((vif And 2) = 0) Then
                                ' E000 100
                                m_description = Description.ENERGY
                                multiplierExponent = (8 + (vif And 1))
                                unit = DlmsUnit.JOULE
                            Else
                                ' E000 101
                                m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E000 11
                            m_description = Description.ENERGY
                            multiplierExponent = (5 + (vif And 3))
                            unit = DlmsUnit.CALORIFIC_VALUE
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
                                m_description = Description.VOLUME
                                multiplierExponent = (2 + (vif And 1))
                                unit = DlmsUnit.CUBIC_METRE
                            Else
                                ' E001 001
                                m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E001 01
                            m_description = Description.REACTIVE_POWER
                            multiplierExponent = (vif And 3)
                            unit = DlmsUnit.VAR
                        End If

                    Else
                        ' E001 1
                        If ((vif And 4) = 0) Then
                            ' E001 10
                            If ((vif And 2) = 0) Then
                                ' E001 100
                                m_description = Description.MASS
                                multiplierExponent = (5 + (vif And 1))
                                unit = DlmsUnit.KILOGRAM
                            Else
                                ' E001 101
                                m_description = Description.REL_HUMIDITY
                                multiplierExponent = (-1 + (vif And 1))
                                unit = DlmsUnit.PERCENTAGE
                            End If

                        Else
                            ' E001 11
                            m_description = Description.NOT_SUPPORTED
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
                                    m_description = Description.VOLUME
                                    multiplierExponent = 0
                                    unit = DlmsUnit.CUBIC_FEET
                                Else
                                    ' E010 0001
                                    m_description = Description.VOLUME
                                    multiplierExponent = -1
                                    unit = DlmsUnit.CUBIC_FEET
                                End If

                            Else
                                ' E010 001
                                ' outdated value !
                                m_description = Description.VOLUME
                                multiplierExponent = (-1 + (vif And 1))
                                unit = DlmsUnit.US_GALLON
                            End If

                        Else
                            ' E010 01
                            If ((vif And 2) = 0) Then
                                ' E010 010
                                If ((vif And 1) = 0) Then
                                    ' E010 0100
                                    ' outdated value !
                                    m_description = Description.VOLUME_FLOW
                                    multiplierExponent = -3
                                    unit = DlmsUnit.US_GALLON_PER_MINUTE
                                Else
                                    ' E010 0101
                                    ' outdated value !
                                    m_description = Description.VOLUME_FLOW
                                    multiplierExponent = 0
                                    unit = DlmsUnit.US_GALLON_PER_MINUTE
                                End If

                            Else
                                ' E010 011
                                If ((vif And 1) = 0) Then
                                    ' E010 0110
                                    ' outdated value !
                                    m_description = Description.VOLUME_FLOW
                                    multiplierExponent = 0
                                    unit = DlmsUnit.US_GALLON_PER_HOUR
                                Else
                                    ' E010 0111
                                    m_description = Description.NOT_SUPPORTED
                                End If

                            End If

                        End If

                    Else
                        ' E010 1
                        If ((vif And 4) = 0) Then
                            ' E010 10
                            If ((vif And 2) = 0) Then
                                ' E010 100
                                m_description = Description.POWER
                                multiplierExponent = (5 + (vif And 1))
                                unit = DlmsUnit.WATT
                            Else
                                If ((vif And 1) = 0) Then
                                    ' E010 1010
                                    m_description = Description.PHASE
                                    multiplierExponent = -1
                                    ' is -1 or 0 correct ??
                                    unit = DlmsUnit.DEGREE
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
                            m_description = Description.FREQUENCY
                            multiplierExponent = (-3 + (vif And 3))
                            unit = DlmsUnit.HERTZ
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
                                m_description = Description.POWER
                                multiplierExponent = (8 + (vif And 1))
                                unit = DlmsUnit.JOULE_PER_HOUR
                            Else
                                ' E011 001
                                m_description = Description.NOT_SUPPORTED
                            End If

                        Else
                            ' E011 01
                            m_description = Description.APPARENT_ENERGY
                            multiplierExponent = (vif And 3)
                            unit = DlmsUnit.VOLT_AMPERE
                        End If

                    Else
                        ' E011 1
                        m_description = Description.NOT_SUPPORTED
                    End If

                End If

            End If

        Else
            ' E1
            If ((vif And 32) = 0) Then
                ' E10
                If ((vif And 16) = 0) Then
                    ' E100
                    m_description = Description.NOT_SUPPORTED
                Else
                    ' E101
                    If ((vif And 8) = 0) Then
                        ' E101 0
                        m_description = Description.NOT_SUPPORTED
                    Else
                        ' E101 1
                        If ((vif And 4) = 0) Then
                            ' E101 10
                            ' outdated value !
                            m_description = Description.FLOW_TEMPERATURE
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E101 11
                            ' outdated value !
                            m_description = Description.RETURN_TEMPERATURE
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_FAHRENHEIT
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
                            m_description = Description.TEMPERATURE_DIFFERENCE
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E110 01
                            ' outdated value !
                            m_description = Description.FLOW_TEMPERATURE
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_FAHRENHEIT
                        End If

                    Else
                        ' E110 1
                        m_description = Description.NOT_SUPPORTED
                    End If

                Else
                    ' E111
                    If ((vif And 8) = 0) Then
                        ' E111 0
                        If ((vif And 4) = 0) Then
                            ' E111 00
                            ' outdated value !
                            m_description = Description.TEMPERATURE_LIMIT
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_FAHRENHEIT
                        Else
                            ' E111 01
                            m_description = Description.TEMPERATURE_LIMIT
                            multiplierExponent = ((vif And 3) - 3)
                            unit = DlmsUnit.DEGREE_CELSIUS
                        End If

                    Else
                        ' E111 1
                        m_description = Description.MAX_POWER
                        multiplierExponent = ((vif And 7) - 3)
                        unit = DlmsUnit.WATT
                    End If

                End If

            End If

        End If

    End Sub
    Private Sub decodeDib(ByVal buffer() As Byte, ByVal i As Integer)

        Dim ff As Integer = (buffer(i) And 48) >> 4  'La funzione verifica il 5 e 6 bit
        Select Case ff
            Case 0  '&B00
                m_functionField = FunctionField.Valore_Istantaneo
            Case 1  '&B01
                m_functionField = FunctionField.Valore_Massimo
            Case 2  '&B10
                m_functionField = FunctionField.Valore_Minimo
            Case 3  '&B11
                m_functionField = FunctionField.Valore_Errore
            Case Else  'impossibile
                m_functionField = Nothing
        End Select

    End Sub
    Private Sub decodeE1(ByVal vif As Integer)
        ' E1
        If ((vif And 32) = 0) Then
            decodeE10(vif)
        Else
            decodeE11(vif)
        End If

    End Sub
    Private Sub decodeE0(ByVal vif As Integer)
        ' E0
        If ((vif And 32) = 0) Then
            decodeE00(vif)
        Else
            decode01(vif)
        End If

    End Sub
    Private Function timeUnitFor(ByVal vif As Byte) As DlmsUnit
        Dim u As Integer = (vif And 3)
        Select Case (u)
            Case 0
                ' E010 1100
                Return DlmsUnit.SECOND
            Case 1
                ' E010 1101
                Return DlmsUnit.MIN
            Case 2
                ' E010 1110
                Return DlmsUnit.HOUR
            Case 3
                ' E010 1111
                Return DlmsUnit.DAY
            Case Else
                Throw New Exception(String.Format("Unknown unit 0x%02X.", u))
        End Select

    End Function
    Private Function unitBiggerFor(ByVal vif As Byte) As DlmsUnit
        Dim u As Integer = (vif And 3)
        Select Case (u)
            Case 0
                ' E110 1100
                Return DlmsUnit.HOUR
            Case 1
                ' E110 1101
                Return DlmsUnit.DAY
            Case 2
                ' E110 1110
                Return DlmsUnit.MONTH
            Case 3
                ' E110 1111
                Return DlmsUnit.YEAR
            Case Else
                Throw New Exception(String.Format("Unknown unit 0x%02X.", u))
        End Select

    End Function
    Private Sub decodeE11(ByVal vif As Integer)
        ' E11
        If ((vif And 16) = 0) Then
            decodeE110(vif)
        Else
            decodeE111(vif)
        End If

    End Sub
    Private Sub decodeE10(ByVal vif As Integer)
        ' E10
        If ((vif And 16) = 0) Then
            ' E100
            If ((vif And 8) = 0) Then
                ' E100 0
                m_description = Description.VOLUME_FLOW_EXT
                multiplierExponent = ((vif And 7) - 7)
                unit = DlmsUnit.CUBIC_METRE_PER_MINUTE
            Else
                ' E100 1
                m_description = Description.VOLUME_FLOW_EXT
                multiplierExponent = ((vif And 7) - 9)
                unit = DlmsUnit.CUBIC_METRE_PER_SECOND
            End If

        Else
            ' E101
            If ((vif And 8) = 0) Then
                ' E101 0
                m_description = Description.MASS_FLOW
                multiplierExponent = ((vif And 7) - 3)
                unit = DlmsUnit.KILOGRAM_PER_HOUR
            Else
                ' E101 1
                If ((vif And 4) = 0) Then
                    ' E101 10
                    m_description = Description.FLOW_TEMPERATURE
                    multiplierExponent = ((vif And 3) - 3)
                    unit = DlmsUnit.DEGREE_CELSIUS
                Else
                    ' E101 11
                    m_description = Description.RETURN_TEMPERATURE
                    multiplierExponent = ((vif And 3) - 3)
                    unit = DlmsUnit.DEGREE_CELSIUS
                End If

            End If

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
                    m_description = Description.ON_TIME
                Else
                    ' E010 01
                    m_description = Description.OPERATING_TIME
                End If

                decodeTimeUnit(vif)
            Else
                ' E010 1
                m_description = Description.POWER
                multiplierExponent = ((vif And 7) - 3)
                unit = DlmsUnit.WATT
            End If

        Else
            ' E011
            If ((vif And 8) = 0) Then
                ' E011 0
                m_description = Description.POWER
                multiplierExponent = (vif And 7)
                unit = DlmsUnit.JOULE_PER_HOUR
            Else
                ' E011 1
                m_description = Description.VOLUME_FLOW
                multiplierExponent = ((vif And 7) - 6)
                unit = DlmsUnit.CUBIC_METRE_PER_HOUR
            End If

        End If

    End Sub
    Private Sub decodeE00(ByVal vif As Integer)
        ' E00
        If ((vif And 16) = 0) Then
            ' E000
            If ((vif And 8) = 0) Then
                ' E000 0
                m_description = Description.ENERGY
                multiplierExponent = ((vif And 7) - 3)
                unit = DlmsUnit.WATT_HOUR
            Else
                ' E000 1
                m_description = Description.ENERGY
                multiplierExponent = (vif And 7)
                unit = DlmsUnit.JOULE
            End If

        Else
            ' E001
            If ((vif And 8) = 0) Then
                ' E001 0
                m_description = Description.VOLUME
                multiplierExponent = ((vif And 7) - 6)
                unit = DlmsUnit.CUBIC_METRE
            Else
                ' E001 1
                m_description = Description.MASS
                multiplierExponent = ((vif And 7) - 3)
                unit = DlmsUnit.KILOGRAM
            End If

        End If

    End Sub
    Private Sub decodeE111(ByVal vif As Integer)
        ' E111
        If ((vif And 8) = 0) Then
            ' E111 0
            If ((vif And 4) = 0) Then
                m_description = Description.AVERAGING_DURATION
            Else
                m_description = Description.ACTUALITY_DURATION
            End If

            decodeTimeUnit(vif)
        Else
            ' E111 1
            If ((vif And 4) = 0) Then
                ' E111 10
                If ((vif And 2) = 0) Then
                    ' E111 100
                    If ((vif And 1) = 0) Then
                        ' E111 1000
                        m_description = Description.FABRICATION_NO
                    Else
                        ' E111 1001
                        m_description = Description.EXTENDED_IDENTIFICATION
                    End If

                Else
                    ' E111 101
                    If ((vif And 1) = 0) Then
                        m_description = Description.ADDRESS
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
                        m_description = Description.NOT_SUPPORTED
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
                        m_description = Description.FUTURE_VALUE
                    Else
                        ' E111 1111
                        m_description = Description.MANUFACTURER_SPECIFIC
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
                m_description = Description.TEMPERATURE_DIFFERENCE
                multiplierExponent = ((vif And 3) - 3)
                unit = DlmsUnit.KELVIN
            Else
                ' E110 01
                m_description = Description.EXTERNAL_TEMPERATURE
                multiplierExponent = ((vif And 3) - 3)
                unit = DlmsUnit.DEGREE_CELSIUS
            End If

        Else
            ' E110 1
            If ((vif And 4) = 0) Then
                ' E110 10
                m_description = Description.PRESSURE
                multiplierExponent = ((vif And 3) - 3)
                unit = DlmsUnit.BAR
            Else
                ' E110 11
                If ((vif And 2) = 0) Then
                    ' E110 110
                    If ((vif And 1) = 0) Then
                        ' E110 1100
                        m_description = Description.DDATE
                        dateTypeG = True
                    Else
                        ' E110 1101
                        m_description = Description.DATE_TIME
                        dateTypeF = True
                    End If

                Else
                    ' E110 111
                    If ((vif And 1) = 0) Then
                        ' E110 1110
                        m_description = Description.HCA
                        unit = DlmsUnit.RESERVED
                    Else
                        m_description = Description.NOT_SUPPORTED
                    End If

                End If

            End If

        End If

    End Sub
    Private Sub decodeTimeUnit(ByVal vif As Integer)
        If ((vif And 2) = 0) Then
            If ((vif And 1) = 0) Then
                unit = DlmsUnit.SECOND
            Else
                unit = DlmsUnit.MIN
            End If

        ElseIf ((vif And 1) = 0) Then
            unit = DlmsUnit.HOUR
        Else
            unit = DlmsUnit.DAY
        End If

    End Sub
    Private Function CopyofRange(ByVal arra As Byte(), FromCopy As Integer, ToCopy As Integer) As Byte()
        Try

            Dim arrb As Byte() = {}
            Array.Resize(arrb, ToCopy - FromCopy)
            Array.Copy(arra, FromCopy, arrb, 0, ToCopy - FromCopy)
            Return arrb

        Catch
            Return Nothing
        End Try
    End Function
    Private Function setBCD(ByVal buffer() As Byte, ByVal i As Integer, ByVal j As Integer) As Integer
        dataValue = CopyofRange(buffer, i, (i + j))
        m_dataValueType = DataValueType.BCD
        Return (i + j)
    End Function
    Private Function getMessageType(ByVal i As Integer) As String

        Dim value As String = String.Empty

        Select Case i
            Case &H44
                value = "Send spontaneous/periodical application data without request (Send /No Reply)"
            Case &H46
                value = "Send manually initiated installation data (Send Installation Request)"
            Case &H47
                value = "Contains no data – signals an empty transmission or provides the opportunity to access the bidirectional meter, between two transmissions of application data."
            Case &H48
                value = "Access demand to master in order to request new important application data (alerts)"
            Case &H0 Or &H10 Or &H20 Or &H30
                value = "Acknowledge the reception of a SND-UD (acknowledgement of transmission only) or response to an REQ-UD1, when no alert happened"
            Case &H1 Or &H11 Or &H21 Or &H31
                value = "Meter reception buffer overflow or Master datagram with invalid C field"
            Case &H8 Or &H18 Or &H28 Or &H38
                value = "Response of application data after a request from master (response of user data)"
            Case Else
                value = "Unknown"
        End Select

        Return value
    End Function
    Private Function getUnit(ByVal i As Integer) As String

        Dim value As String = String.Empty

        Select Case i
            Case 0
                value = ""
            Case DlmsUnit.YEAR
                value = "a"
            Case DlmsUnit.MONTH
                value = "mo"
            Case DlmsUnit.WEEK
                value = "wk"
            Case DlmsUnit.DAY
                value = "d"
            Case DlmsUnit.HOUR
                value = "h"
            Case DlmsUnit.MIN
                value = "min"
            Case DlmsUnit.SECOND
                value = "s"
            Case DlmsUnit.DEGREE
                value = "°"
            Case DlmsUnit.DEGREE_CELSIUS
                value = "°C"
            Case DlmsUnit.CURRENCY
                value = String.Empty
            Case DlmsUnit.METRE
                value = "m"
            Case DlmsUnit.METRE_PER_SECOND
                value = "m/s"
            Case DlmsUnit.CUBIC_METRE
                value = "m³"
            Case DlmsUnit.CUBIC_METRE_CORRECTED
                value = "m³"
            Case DlmsUnit.CUBIC_METRE_PER_HOUR
                value = "m³/h"
            Case DlmsUnit.CUBIC_METRE_PER_HOUR_CORRECTED
                value = "m³/h"
            Case DlmsUnit.CUBIC_METRE_PER_DAY
                value = "m³/d"
            Case DlmsUnit.CUBIC_METRE_PER_DAY_CORRECTED
                value = "m³/d"
            Case DlmsUnit.LITRE
                value = "l"
            Case DlmsUnit.KILOGRAM
                value = "kg"
            Case DlmsUnit.NEWTON
                value = "N"
            Case DlmsUnit.NEWTONMETER
                value = "Nm"
            Case DlmsUnit.PASCAL
                value = "Pa"
            Case DlmsUnit.BAR
                value = "bar"
            Case DlmsUnit.JOULE
                value = "J"
            Case DlmsUnit.JOULE_PER_HOUR
                value = "J/h"
            Case DlmsUnit.WATT
                value = "W"
            Case DlmsUnit.VOLT_AMPERE
                value = "VA"
            Case DlmsUnit.VAR
                value = "var"
            Case DlmsUnit.WATT_HOUR
                value = "Wh"
            Case DlmsUnit.VOLT_AMPERE_HOUR
                value = "VAh"
            Case DlmsUnit.VAR_HOUR
                value = "varh"
            Case DlmsUnit.AMPERE
                value = "A"
            Case DlmsUnit.COULOMB
                value = "C"
            Case DlmsUnit.VOLT
                value = "V"
            Case DlmsUnit.VOLT_PER_METRE
                value = "V/m"
            Case DlmsUnit.FARAD
                value = "F"
            Case DlmsUnit.OHM
                value = "Ohm"
            Case DlmsUnit.OHM_METRE
                value = "Ohm m²/m"
            Case DlmsUnit.WEBER
                value = "Wb"
            Case DlmsUnit.TESLA
                value = "T"
            Case DlmsUnit.AMPERE_PER_METRE
                value = "A/m"
            Case DlmsUnit.HENRY
                value = "H"
            Case DlmsUnit.HERTZ
                value = "Hz"
            Case DlmsUnit.ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE
                value = "1/=Wh"
            Case DlmsUnit.REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE
                value = "1/=varh"
            Case DlmsUnit.APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE
                value = "1=VAh"
            Case DlmsUnit.VOLT_SQUARED_HOURS
                value = "V²h"
            Case DlmsUnit.AMPERE_SQUARED_HOURS
                value = "A²h"
            Case DlmsUnit.KILOGRAM_PER_SECOND
                value = "kg/s"
            Case DlmsUnit.SIEMENS
                value = "S"
            Case DlmsUnit.KELVIN
                value = "K"
            Case DlmsUnit.VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE
                value = "1/=V²h)"
            Case DlmsUnit.AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE
                value = "1/=A²h"
            Case DlmsUnit.METER_CONSTANT_OR_PULSE_VALUE
                value = "1/m³"
            Case DlmsUnit.PERCENTAGE
                value = "%"
            Case DlmsUnit.AMPERE_HOUR
                value = "Ah"
            Case DlmsUnit.ENERGY_PER_VOLUME
                value = "Wh/m³"
            Case DlmsUnit.CALORIFIC_VALUE
                value = "J/m³"
            Case DlmsUnit.MOLE_PERCENT
                value = "Mol %"
            Case DlmsUnit.MASS_DENSITY
                value = "g/m³"
            Case DlmsUnit.PASCAL_SECOND
                value = "Pa s"
            Case DlmsUnit.SPECIFIC_ENERGY
                value = "J/kg"
            Case DlmsUnit.SIGNAL_STRENGTH
                value = "dBm"
            Case DlmsUnit.SIGNAL_STRENGTH_MICROVOLT
                value = "dBµv"
            Case DlmsUnit.LOGARITHMIC
                value = "dB"
            Case DlmsUnit.RESERVED
                value = ""
            Case DlmsUnit.OTHER_UNIT
                value = "other"
            Case DlmsUnit.COUNT
                value = "count"
            Case DlmsUnit.CUBIC_METRE_PER_SECOND
                value = "m³/s"
            Case DlmsUnit.CUBIC_METRE_PER_MINUTE
                value = "m³/min"
            Case DlmsUnit.KILOGRAM_PER_HOUR
                value = "kg/h"
            Case DlmsUnit.CUBIC_FEET
                value = "cft"
            Case DlmsUnit.US_GALLON
                value = "Impl. gal."
            Case DlmsUnit.US_GALLON_PER_MINUTE
                value = "Impl. gal./min"
            Case DlmsUnit.US_GALLON_PER_HOUR
                value = "Impl. gal./h"
            Case DlmsUnit.DEGREE_FAHRENHEIT
                value = "°F"

        End Select

        Return value
    End Function
    Private Function DecodeAES(frame As String) As String

        '                          0                           1             2            3          4
        '                          1234 5678                  90123456   78 90 12 34 567890 1234567890
        ' Dim frame_d As String = "8E44 C514                  36599021   00 07 7A 34 0B8005 E90C5A2416BF072DC29AEDB12C04381A273B3A20F8F8B2945E959BB4A129161B998066F047E64CE6F8BAFDEF35125EF437F261F8CD04A7A0077213C7FBE453EABED82A2298964AAD52F420333D43F89F4DC5E4AB7D8C427D8E166ABBE36D00EACF8BBA5ACDB3064A6D8324F9E076B41846D8E052B18DB3E04FB24AC0777B3BA0"

        '                          0          1          2           3             4           5
        '                          1234 5678 90123456 7890 12 34567890 1234 56 78 90 123456 7890
        ' Dim frame As String = "  9644 C514 84058042 0107 72 21436587 C514 00 07 6D 0B8005 6BEAEC791CB1E911D3442070F2EE1961B7E3D8917683E53C2045CF57AFF24DCB5F8DE5550299EF614DFD74F210D72AE6CC4728C31F2A0E0D25CF1C69F57DD57420CCCF42299BBEEA1528A9F21E70F68024CC3FD4C37DA62C4EA0FDFCB91C73EB1A933D59C5599EC7A6B3718128A0A572C3D61C4567323AEB79497349A191B66F5A00ECE0530D32"
        ' Dim aesBlock As String = "  6BEAEC791CB1E911D3442070F2EE1961B7E3D8917683E53C2045CF57AFF24DCB5F8DE5550299EF614DFD74F210D72AE6CC4728C31F2A0E0D25CF1C69F57DD57420CCCF42299BBEEA1528A9F21E70F68024CC3FD4C37DA62C4EA0FDFCB91C73EB1A933D59C5599EC7A6B3718128A0A572C3D61C4567323AEB79497349A191B66F"
        ' Dim should_be As String = "2f2f426c000044130000000001fd170884011300000000c401130000000084021300000000c402130000000084031300000000c403130000000084041300000000c404130000000084051300000000c405130000000084061300000000c406130000000084071300000000c4071300000000840813000000002f2f2f2f2f2f2f"

        Dim frame_type As String = ""

        Dim aesBlock As String = ""
        Dim address As String = ""
        Dim manuf As String = ""
        Dim accessNumber As String = ""
        Dim medium As String = ""
        Dim version As String = ""
        Dim status As String = ""
        Dim x, y As String
        Dim b1, b2, b3, b4 As String
        Dim s() As Byte
        Dim result As Byte()

        x = ""
        y = ""

        ' frame = "9644C5149410504301077294105043C5140007170B8105750831D30960315F4B7B158BD8D553C6A355B3049AE1611A99418DD54E0AA3BD91318059F439946AFCE30D14F8397B97407BC8D66075D4DC340844B0CCAC738FB8D27B6C0471C6723D1EDBB47D6E51077FFF7A2F41F2CB23CC86E957AF913DA21FA596E491D06409700D90FABEE06404700575D954800375D95590FABEE00402"
        ' decodificato diventa "2f2f426cdf1c44130000000001fd174884011301000000c401130100000084021301000000c402130000000084031300000000c403130000000084041300000000c404130000000084051300000000c405130000000084061300000000c406131efa43d3c7705b8b5ad1d64691064a79665083feff7d5111195a2c2c3bb45746"

        'Dim telegramma As String = "5244C51445195824010D7A4F2000202F2F046D2B275C2B0406000000008410060000000001FD1700041501000000043B00000000042B00000000025F140004610300000084401410000000848040140000000000"


        If frame <> "" Then
            frame_type = Mid(frame, 21, 2)
            Select Case frame_type
                Case "7A"
                    b1 = Mid(frame, 9 + OFFSET_BYTE1, 2)
                    b2 = Mid(frame, 9 + OFFSET_BYTE2, 2)
                    b3 = Mid(frame, 9 + OFFSET_BYTE3, 2)
                    b4 = Mid(frame, 9 + OFFSET_BYTE4, 2)
                    address = b4 & b3 & b2 & b1
                    manuf = Mid(frame, 5, 4)
                    accessNumber = Mid(frame, 23, 2)
                    medium = Mid(frame, 19, 2)
                    version = Mid(frame, 17, 2)
                    status = Mid(frame, 25, 2)

                    s = SoapHexBinary.Parse(status).Value
                    If frame.Length > 256 And frame.Length <= 512 Then
                        If CBool(s(0) And &H20) = True Then     ' Check bit 5 status
                            aesBlock = Strings.Mid(frame, 31, frame.Length).PadRight(512, "0")      ' 1 = Frame compatta
                        Else
                            aesBlock = Strings.Mid(frame, 31, frame.Length).PadRight(512, "0")      ' 0 = Frame lunga
                        End If
                    ElseIf frame.Length <= 256 Then
                        If CBool(s(0) And &H20) = True Then     ' Check bit 5 status
                            aesBlock = Strings.Mid(frame, 31, frame.Length).PadRight(256, "0")      ' 1 = Frame compatta
                        Else
                            aesBlock = Strings.Mid(frame, 31, frame.Length).PadRight(256, "0")      ' 0 = Frame lunga
                        End If
                    End If

                Case "72"
                    b1 = Mid(frame, 23 + OFFSET_BYTE1, 2)
                    b2 = Mid(frame, 23 + OFFSET_BYTE2, 2)
                    b3 = Mid(frame, 23 + OFFSET_BYTE3, 2)
                    b4 = Mid(frame, 23 + OFFSET_BYTE4, 2)
                    address = b4 & b3 & b2 & b1
                    manuf = Mid(frame, 31, 4)
                    accessNumber = Mid(frame, 39, 2)
                    medium = Mid(frame, 37, 2)
                    version = Mid(frame, 35, 2)
                    status = Mid(frame, 41, 2)

                    s = SoapHexBinary.Parse(status).Value
                    If frame.Length > 256 And frame.Length <= 512 Then
                        If CBool(s(0) And &H20) = True Then     ' Check bit 5 status
                            aesBlock = Strings.Mid(frame, 47, frame.Length).PadRight(512, "0")      ' 1 = Frame compatta
                        Else
                            aesBlock = Strings.Mid(frame, 47, frame.Length).PadRight(512, "0")      ' 0 = Frame lunga
                        End If
                    ElseIf frame.Length <= 256 Then
                        If CBool(s(0) And &H20) = True Then     ' Check bit 5 status
                            aesBlock = Strings.Mid(frame, 47, frame.Length).PadRight(256, "0")      ' 1 = Frame compatta
                        Else
                            aesBlock = Strings.Mid(frame, 47, frame.Length).PadRight(256, "0")      ' 0 = Frame lunga
                        End If
                    End If
                Case Else
                    Return ""
            End Select
            Try
                result = WMBUSDecrypt.decryptBlock(SoapHexBinary.Parse(address).Value, SoapHexBinary.Parse(manuf).Value, SoapHexBinary.Parse(version).Value(0), SoapHexBinary.Parse(medium).Value(0), SoapHexBinary.Parse(accessNumber).Value(0), SoapHexBinary.Parse(aesBlock).Value)

                x = ""
                y = result.Length

                For m = 0 To y - 1
                    x = x & Hex(result(m)).PadLeft(2, "0").ToLower.ToString
                Next


            Catch ex As Exception
                MsgBox(ex.Message) ' "Errore di decodifica AES!!!"
                Return ""
            End Try

        End If

        DecodeAES = x

    End Function

End Class
Public Class DecodedDeviceClass
    Public Property manufacturerId As String
    Public Property manufacturerDetails As String
    Public Property deviceId As String
    Public Property version As Integer
    Public Property deviceType As String
    Public Property accessNumber As String
    Public Property status As String
    Public Property signature As String
    Public Property messageTypeID As String

    Public DecodedDataField As DecodedDataFields() = {}
    Public Sub New()
        Me.manufacturerId = ""
        Me.manufacturerDetails = ""
        Me.deviceId = ""
        Me.version = 0
        Me.deviceType = ""
    End Sub
End Class
Public Structure DecodedDataFields
    Public m_description As String
    Public m_functionField As String
    Public m_datavaluetype As String
    Public subunit As String
    Public tariff As String
    Public storagenumber As String
    Public datavalue As Object
    Public datavalueSimplified As String
    Public unitDescription As String
    Public unitSymbol As String
    Public Property multiplierexponent As Integer
End Structure
