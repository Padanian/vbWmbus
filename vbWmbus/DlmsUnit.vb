Public Class DlmsUnit




    YEAR(1,"a"),
    MONTH(2, "mo"),
    WEEK(3, "wk"),
    DAY(4, "d"),
    HOUR(5, "h"),
    MIN(6, "min"),
    SECOND(7, "s"),
    DEGREE(8, "°"),
    DEGREE_CELSIUS(9, "°C"),
    CURRENCY(10, ""),
    METRE(11, "m"),
    METRE_PER_SECOND(12, "m/s"),
    CUBIC_METRE(13, "m³"),
    CUBIC_METRE_CORRECTED(14, "m³"),
    CUBIC_METRE_PER_HOUR(15, "m³/h"),
    CUBIC_METRE_PER_HOUR_CORRECTED(16, "m³/h"),
    CUBIC_METRE_PER_DAY(17, "m³/d"),
    CUBIC_METRE_PER_DAY_CORRECTED(18, "m³/d"),
    LITRE(19, "l"),
    KILOGRAM(20, "kg"),
    NEWTON(21, "N"),
    NEWTONMETER(22, "Nm"),
    PASCAL(23, "Pa"),
    BAR(24, "bar"),
    JOULE(25, "J"),
    JOULE_PER_HOUR(26, "J/h"),
    WATT(27, "W"),
    VOLT_AMPERE(28, "VA"),
    VAR(29, "var"),
    WATT_HOUR(30, "Wh"),
    VOLT_AMPERE_HOUR(31, "VAh"),
    VAR_HOUR(32, "varh"),
    AMPERE(33, "A"),
    COULOMB(34, "C"),
    VOLT(35, "V"),
    VOLT_PER_METRE(36, "V/m"),
    FARAD(37, "F"),
    OHM(38, "Ohm"),
    OHM_METRE(39, "Ohm m²/m"),
    WEBER(40, "Wb"),
    TESLA(41, "T"),
    AMPERE_PER_METRE(42, "A/m"),
    HENRY(43, "H"),
    HERTZ(44, "Hz"),
    ACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(45, "1/(Wh)"),
    REACTIVE_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(46, "1/(varh)"),
    APPARENT_ENERGY_METER_CONSTANT_OR_PULSE_VALUE(47, "1(VAh)"),
    VOLT_SQUARED_HOURS(48, "V²h"),
    AMPERE_SQUARED_HOURS(49, "A²h"),
    KILOGRAM_PER_SECOND(50, "kg/s"),
    SIEMENS(51, "S"),
    KELVIN(52, "K"),
    VOLT_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE(53, "1/(V²h)"),
    AMPERE_SQUARED_HOUR_METER_CONSTANT_OR_PULSE_VALUE(54, "1/(A²h)"),
    METER_CONSTANT_OR_PULSE_VALUE(55, "1/m³"),
    PERCENTAGE(56, "%"),
    AMPERE_HOUR(57, "Ah"),

    ENERGY_PER_VOLUME(60, "Wh/m³"),
    CALORIFIC_VALUE(61, "J/m³"),
    MOLE_PERCENT(62, "Mol %"),
    MASS_DENSITY(63, "g/m³"),
    PASCAL_SECOND(64, "Pa s"),
    SPECIFIC_ENERGY(65, "J/kg"),

    SIGNAL_STRENGTH(70, "dBm"),
    SIGNAL_STRENGTH_MICROVOLT(71, "dBµv"),
    LOGARITHMIC(72, "dB"),

    RESERVED(253, ""),
    OTHER_UNIT(254, "other"),
    COUNT(255, "count"),

    // Not mentioned in 62056, added for MBus
    CUBIC_METRE_PER_SECOND(150, "m³/s"),
    CUBIC_METRE_PER_MINUTE(151, "m³/min"),
    KILOGRAM_PER_HOUR(152, "kg/h"),
    CUBIC_FEET(153, "cft"),
    US_GALLON(154, "Impl. gal."),
    US_GALLON_PER_MINUTE(155, "Impl. gal./min"),
    US_GALLON_PER_HOUR(156, "Impl. gal./h"),
    DEGREE_FAHRENHEIT(157, "°F");
End Enum
    Public Function getId() As Integer
        Return ID
    End Function

    Public Function getUnit() As String
        Return unit
    End Function

    Public Shared Function getInstance(ByVal id As Integer) As DlmsUnit
        Dim enumInstance As DlmsUnit = idMap.get(id)
        If (enumInstance Is Nothing) Then
            enumInstance = DlmsUnit.RESERVED
        End If

        Return enumInstance
    End Function
End Class
