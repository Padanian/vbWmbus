Public Class EnumerazioniWMBus
    Public Enum DataValueType
        _LONG
        _DOUBLE
        _DATE
        _STRING
        _BCD
        _NONE
    End Enum
    Public Enum FunctionField
        Valore_Istantaneo
        Valore_Massimo
        Valore_Minimo
        Valore_Errore
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
    Public Function getUnit(ByVal i As Integer) As String

        Dim value As String = String.Empty

        Select Case i
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
