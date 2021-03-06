/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SECTIONS: constant-literals, real-literals
 * PARAGRAPH: 4
 * SENTENCE: [1] The digits of the whole-number part or the fraction part or the exponent may be optionally separated by underscores, but an underscore may not be placed between, before, or after these parts.
 * NUMBER: 5
 * DESCRIPTION: Real literals with an omitted fraction part and underscores in a whole-number part, a fraction part and an exponent part.
 */

val value_1 = 0_0F
val value_2 = 0_0E-0_0F
val value_3 = 0_0E-0_0
val value_4 = 0_0____0f
val value_5 = 0_0____0e-0f
val value_6 = 0_0_0_0F
val value_7 = 0_0_0_0E-0_0_0_0F
val value_8 = 0000000000000000000_______________0000000000000000000f
val value_9 = 0000000000000000000_______________0000000000000000000e+0f
val value_10 = 0000000000000000000_______________0000000000000000000E-0

val value_11 = 2___2e-2___2f
val value_12 = 33_3E0_0F
val value_13 = 4_444E-4_444f
val value_14 = 55_5_55F
val value_15 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666f
val value_16 = 666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666E-10
val value_17 = 7_7_7_7_7_7_7f
val value_18 = 8888888________8e-9000000_0
val value_19 = 9________9_______9______9_____9____9___9__9_9F

val value_20 = 1__2_3__4____5_____6__7_89f
val value_21 = 2__34567e8
val value_22 = 345_6E+9_7F

fun box(): String? {
    val value_23 = 45_____________________________________________________________6E-12313413_4
    val value_24 = 5_______________________________________________________________________________________________________________________________________________________________________________________5f
    val value_25 = 6__________________________________________________54F
    val value_26 = 76_5___4e3___________33333333
    val value_27 = 876543_____________________________________________________________2f
    val value_28 = 9_8__7654__3_21F

    val value_29 = 000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0__0F
    val value_30 = 0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f
    val value_31 = 33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E-1_0_0

    if (value_1.compareTo(0_0F) != 0 || value_1.compareTo(0.0) != 0) return null
    if (value_2.compareTo(0_0E-0_0F) != 0 || value_2.compareTo(0.0) != 0) return null
    if (value_3.compareTo(0_0E-0_0) != 0 || value_3.compareTo(0.0f) != 0) return null
    if (value_4.compareTo(0_0____0f) != 0 || value_4.compareTo(0.0) != 0) return null
    if (value_5.compareTo(0_0____0e-0f) != 0 || value_5.compareTo(0.0f) != 0) return null
    if (value_6.compareTo(0_0_0_0F) != 0 || value_6.compareTo(0.0) != 0) return null

    if (value_7.compareTo(0_0_0_0E-0_0_0_0F) != 0 || value_7.compareTo(0.0f) != 0) return null
    if (value_8.compareTo(0000000000000000000_______________0000000000000000000f) != 0 || value_8.compareTo(0.0) != 0) return null
    if (value_9.compareTo(0000000000000000000_______________0000000000000000000e+0f) != 0 || value_9.compareTo(0.0F) != 0) return null
    if (value_10.compareTo(0000000000000000000_______________0000000000000000000E-0) != 0 || value_10.compareTo(0.0) != 0) return null
    if (value_11.compareTo(2___2e-2___2f) != 0 || value_11.compareTo(2.2E-21f) != 0) return null
    if (value_12.compareTo(33_3E0_0F) != 0 || value_12.compareTo(333.0F) != 0) return null
    if (value_13.compareTo(4_444E-4_444f) != 0 || value_13.compareTo(0.0) != 0) return null
    if (value_14.compareTo(55_5_55F) != 0 || value_14.compareTo(55555.0F) != 0) return null

    if (value_15.compareTo(666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666f) != 0 || value_15.compareTo(666666.0f) != 0) return null
    if (value_16.compareTo(666____________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________666E-10) != 0 || value_16.compareTo(6.66666E-5) != 0) return null
    if (value_17.compareTo(7_7_7_7_7_7_7f) != 0 || value_17.compareTo(7777777.0F) != 0) return null
    if (value_18.compareTo(8888888________8e-9000000_0) != 0 || value_18.compareTo(0.0) != 0) return null
    if (value_19.compareTo(9________9_______9______9_____9____9___9__9_9F) != 0 || value_19.compareTo(1.0E9f) != 0) return null
    if (value_20.compareTo(1__2_3__4____5_____6__7_89f) != 0 || value_20.compareTo(1.23456792E8F) != 0) return null
    if (value_21.compareTo(2__34567e8) != 0 || value_21.compareTo(2.34567E13) != 0) return null
    if (value_22.compareTo(345_6E+9_7F) != 0 || value_22.compareTo(Float.POSITIVE_INFINITY) != 0) return null
    if (value_23.compareTo(45_____________________________________________________________6E-12313413_4) != 0 || value_23.compareTo(0.0) != 0) return null
    if (value_24.compareTo(5_______________________________________________________________________________________________________________________________________________________________________________________5f) != 0 || value_24.compareTo(55.0F) != 0) return null
    if (value_25.compareTo(6__________________________________________________54F) != 0 || value_25.compareTo(654.0f) != 0) return null
    if (value_26.compareTo(76_5___4e3___________33333333) != 0 || value_26.compareTo(Double.POSITIVE_INFINITY) != 0) return null
    if (value_27.compareTo(876543_____________________________________________________________2f) != 0 || value_27.compareTo(8765432.0F) != 0) return null
    if (value_28.compareTo(9_8__7654__3_21F) != 0 || value_28.compareTo(9.8765434E8f) != 0) return null
    if (value_29.compareTo(000000000000000000000000000000000000000000000000000000000000000000000000000000000000000e0__0F) != 0 || value_29.compareTo(0.0) != 0) return null
    if (value_30.compareTo(0___000000000000000000000000000000000000000000000000000000000000000000000000000000000000000f) != 0 || value_30.compareTo(0.0) != 0) return null
    if (value_31.compareTo(33333333333333333333333333333333333333333333333_33333333333333333333333333333333333333333E-1_0_0) != 0 || value_31.compareTo(3.3333333333333334E-13) != 0) return null

    return "OK"
}
