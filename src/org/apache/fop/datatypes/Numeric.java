/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.datatypes;

import java.lang.Number;

import org.apache.fop.fo.expr.AbstractPropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.Properties;


/**
 * An abstraction of "numeric" values as defined by the XSL FO Specification.
 * Numerics include absolute numbers, absolute lengths, relative lengths
 * (percentages and ems), angle, time and frequency.
 *
 * Relative lengths are converted immediately to a pure numeric factor, i.e.
 * an absolute number (a number with unit power zero.)  They retain a
 * baseunit of PERCENTAGE or EMS respectively.
 *
 * Relative lengths resolve to absolute lengths as soon as they are involved
 * in a multop with any Numeric with a baseunit of MILLIPOINTS.  It is illegal
 * for them to be involved in multops with Numerics of other baseunits.
 *
 * Therefore, only a number, its power and its baseunit need be provided for
 * in this class.
 *
 * All numeric values are represented as a value and a unit raised to a
 * power.  For absolute numbers, including relative lengths, the unit power is
 * zero.
 *
 * Whenever the power associated with a number is non-zero, it is a length,
 * angle, time or frequency.
 *
 * It is an error for the end result of an expression to be a numeric with
 * a power other than 0 or 1. (Rec. 5.9.6)
 *
 * Operations defined on combinations of the types are (where
 *   unit      = ( MILLIPOINTS | HERTZ | MILLISECS | DEGREES )
 *   length    = MILLIPOINTS
 *   number    = NUMBER
 *   relunit   = ( PERCENTAGE | EMS )
 *   notnumber = ( unit | relunit )
 *   absunit   = ( number | unit )
 *   notlength = ( HERTZ | MILLISECS | DEGREES )
 *   numeric   = ( number | notnumber )
 * )
 * numeric^n   addop  numeric^m   = Illegal
 * numeric1    addop  numeric2    = Illegal
 * numeric1^n  addop  numeric1^n  = numeric1^n   includes number + number
 *
 * number      multop anyunit     = anyunit      universal multiplier
 *                                               includes number * relunit
 * unit1       multop unit2       = Illegal
 * relunit     multop notlength   = Illegal
 * relunit     multop relunit     = Illegal
 *
 * unit1^n     multop unit1^m     = unit1^(n+m)  includes number * number
 *                                               excludes relunit* relunit
 * relunit     multop length      = length
 *
 * <i>Numeric</i>s are changeable, as the above table shows.  A numeric
 * created as a <i>Time</i> in seconds to power 1, e.g., if divided by another
 * <i>Time</i> in seconds to power 1 becomes a number.  if it is then
 * multiplied by a <i>Length</i> in points to power 1, it becomes a
 * <i>Length</i> in points.
 *
 * In fact, all lengths are maintained internally
 * in millipoints.  Each of the non-number <i>Numeric</i> types is maintained
 * internally in a single baseunit.  These are:<br/>
 * MILLIPOINTS<br/>
 * HERTZ<br/>
 * MILLISECS<br/>
 * DEGREES<br/>
 */
public class Numeric extends AbstractPropertyValue implements Cloneable {

    /**
     * Integer constant encoding a valid Numeric subclass
     * base unit
     */
    public static final int
             NUMBER = 1
        ,PERCENTAGE = 2
               ,EMS = 4
       ,MILLIPOINTS = 8
             ,HERTZ = 16
         ,MILLISECS = 32
           ,DEGREES = 64

     ,LAST_BASEUNIT = DEGREES
                  ;

    /**
     * Integer constants for the subunits of numbers.
     */
    public static final int NUMERIC = NUMBER | PERCENTAGE | EMS;

    /**
     * Integer constants for the subunits of relative lengths.
     */
    public static final int REL_LENGTH = PERCENTAGE | EMS;

    /**
     * Integer constants for the named units.
     */
    public static final int UNIT = MILLIPOINTS | HERTZ | MILLISECS | DEGREES;

    /**
     * Integer constants for the named units not a length.
     */
    public static final int NOT_LENGTH = HERTZ | MILLISECS | DEGREES;

    /**
     * Integer constants for the absolute-valued units.
     */
    public static final int ABS_UNIT = NUMBER | UNIT;

    /**
     * Integer constants for non-numbers.
     */
    public static final int NOT_NUMBER = UNIT | REL_LENGTH;

    /**
     * Integer constants for distances.
     */
    public static final int DISTANCE = MILLIPOINTS | REL_LENGTH;

    /**
     * The numerical contents of this instance.
     */
    protected double value;

    /**
     * The power to which the UNIT (not the number) is raised
     */
    protected int power;

    /**
     * The current baseunit of this instance.
     * One of the constants defined here.  Defaults to millipoints.
     */
    private int baseunit = MILLIPOINTS;

    /**
     * The baseunit in which this <tt>Numeric</tt> was originally defined.
     */
    private int originalBaseUnit = MILLIPOINTS;

    /**
     * The actual unit in which this <tt>Numeric</tt> was originally defined.
     * This is a constant defined in each of the original unit types.
     */
    private int originalUnit = Length.PT;

    /**
     * Construct a fully specified <tt>Numeric</tt> object.
     * @param property <tt>int</tt> index of the property.
     * @param value the actual value.
     * @param baseunit the baseunit for this <tt>Numeric</tt>.
     * @param power The dimension of the value. 0 for a Number, 1 for a Length
     * (any type), >1, <0 if Lengths have been multiplied or divided.
     * @param unit <tt>int</tt> enumeration of the subtype of the
     * <i>baseunit</i> in which this <i>Numeric</i> is being defined.
     */
    protected Numeric
        (int property, double value, int baseunit, int power, int unit)
        throws PropertyException
    {
        super(property);
        // baseunit must be a power of 2 <= the LAST_BASEUNIT
        if ((baseunit & (baseunit - 1)) != 0
            || baseunit > LAST_BASEUNIT)
            throw new PropertyException
                    ("Invalid baseunit: " + baseunit);
        if ((baseunit & NUMERIC) != 0 && power != 0)
            throw new PropertyException
                    ("Invalid power for NUMERIC: " + power);

        this.value = value;
        this.power = power;
        this.baseunit = baseunit;
        originalBaseUnit = baseunit;
        originalUnit = unit;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
    }

    /**
     * Construct a fully specified <tt>Numeric</tt> object.
     * @param propertyName <tt>String</tt> name of the property.
     * @param value the actual value.
     * @param baseunit the baseunit for this <tt>Numeric</tt>.  Because a
     * <i>power</i> is being specified, <i>NUMBER</i> baseunits are invalid.
     * @param power The dimension of the value. 0 for a Number, 1 for a Length
     * (any type), >1, <0 if Lengths have been multiplied or divided.
     * @param unit <tt>int</tt> enumeration of the subtype of the
     * <i>baseunit</i> in which this <i>Numeric</i> is being defined.
     */
    protected Numeric (String propertyName, double value, int baseunit,
                       int power, int unit)
        throws PropertyException
    {
        this(PropertyConsts.getPropertyIndex(propertyName),
             value, baseunit, power, unit);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>double</tt>.
     * @param propertyName <tt>String</tt> property name.
     * @param value the number as a <tt>double</tt>.
     */
    public Numeric(String propertyName, double value)
        throws PropertyException
    {
         this(PropertyConsts.getPropertyIndex(propertyName), value);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>double</tt>.
     * @param property <tt>int</tt> property index.
     * @param value the number as a <tt>double</tt>.
     */
    public Numeric(int property, double value) throws PropertyException {
        this(property, value, NUMBER, 0, 0);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>long</tt>.
     * @param propertyName <tt>String</tt> property name.
     * @param value the number as a <tt>long</tt>.
     */
    public Numeric(String propertyName, long value)
        throws PropertyException
    {
        this(propertyName, (double)value);
    }

    /**
     * Construct a <tt>Numeric</tt> object of dimension 0 from a
     * <tt>long</tt>.
     * @param property <tt>int</tt> property index.
     * @param value the number as a <tt>long</tt>.
     */
    public Numeric(int property, long value)throws PropertyException {
        this(property, (double)value);
    }

    /**
     * Construct a <tt>Numeric</tt> object from a Number.
     * @param propertyName <tt>String</tt> property name.
     * @param num an absolute number.
     */
    public Numeric(String propertyName, Number num)
        throws PropertyException
    {
        this(propertyName, num.doubleValue());
    }

    /**
     * Construct a <tt>Numeric</tt> object from a Number.
     * @param property <tt>int</tt> property index.
     * @param num an absolute number.
     */
    public Numeric(int property, Number num) throws PropertyException {
        this(property, num.doubleValue());
    }

    /**
     * @return <tt>double</tt> value of the <i>Numeric</i>.
     */
    public double getValue() {
        return value;
    }

    /**
     * @return <tt>int</tt> unit power of this <i>Numeric</i>.
     */
    public int getPower() {
        return power;
    }

    /**
     * @return <tt>int</tt> current baseunit of this <i>Numeric</i>.
     */
    public int getBaseunit() {
        return baseunit;
    }

    /**
     * @return <tt>int</tt> original base unit of this <i>Numeric</i>.
     */
    public int getOriginalBaseUnit() {
        return originalBaseUnit;
    }

    /**
     * @return <tt>int</tt> original unit in which this <i>Numeric</i> was
     * defined.  Value is defined in terms of the originalBaseUnit type.
     */
    public int getOriginalUnit() {
        return originalUnit;
    }

    /**
     * Validate this <i>Numeric</i>.
     * @exception PropertyException if this numeric is invalid for
     * the associated property.
     */
    public void validate() throws PropertyException {
        switch (baseunit) {
        case NUMBER:
            if (power != 0)
                throw new PropertyException
                        ("Attempt to validate Numeric with unit power of "
                         + power);
            super.validate(Properties.NUMBER);
            break;
        case PERCENTAGE:
            if (power != 0)
                throw new PropertyException
                        ("Attempt to validate Percentage with unit power of "
                         + power);
            super.validate(Properties.PERCENTAGE);
            break;
        case MILLIPOINTS:
            super.validate(Properties.LENGTH);
            if (power != 1)
                throw new PropertyException
                        ("Length with unit power " + power);
            break;
        case HERTZ:
            super.validate(Properties.FREQUENCY);
            if (power != 1)
                throw new PropertyException
                        ("Frequency with unit power " + power);
            break;
        case MILLISECS:
            super.validate(Properties.TIME);
            if (power != 1)
                throw new PropertyException
                        ("Time with unit power " + power);
            break;
        case DEGREES:
            super.validate(Properties.ANGLE);
            if (power != 1)
                 throw new PropertyException
                         ("Angle with unit power " + power);
            break;
        default: 
            throw new PropertyException
                    ("Unrecognized baseunit type: " + baseunit);
        }
    }

    /**
     * This object has a NUMERIC type if it is a NUMBER, EMS or PERCENTAGE
     * type.
     */
    public boolean isNumeric() {
        return (baseunit & NUMERIC) != 0;
    }

    /**
     * This object is a number if the baseunit is NUMBER.  Power is
     * guaranteed to be zero for NUMBER baseunit.
     */
    public boolean isNumber() {
        return (baseunit == NUMBER);
    }

    /**
     * This object is an EMS factor is the baseunit is EMS.  Power is
     * guaranteed to be zero for EMS baseunit.
     */
    public boolean isEms() {
        return (baseunit == EMS);
    }

    /**
     * This object is a percentage factor if the baseunit is PERCENTAGE.
     * Power is guaranteed to be zero for PERCENTAGE baseunit.
     */
    public boolean isPercentage() {
        return (baseunit == PERCENTAGE);
    }

    /**
     * This object is a length in millipoints.
     */
    public boolean isLength() {
        return (baseunit == MILLIPOINTS && power == 1);
    }

    /**
     * This object is a distance; a absolute or relative length
     */
    public boolean isDistance() {
        return (baseunit & DISTANCE) != 0;
    }

    /**
     * This object is a time in milliseconds.
     */
    public boolean isTime() {
        return (baseunit == MILLISECS && power == 1);
    }

    /**
     * This object is a frequency in hertz.
     */
    public boolean isFrequency() {
        return (baseunit == HERTZ && power == 1);
    }

    /**
     * This object is an angle in degrees.
     */
    public boolean isAngle() {
        return (baseunit == DEGREES && power == 1);
    }

    /**
     * Return this <tt>Numeric</tt> converted (if necessary) to a
     * <tt>double</tt>.  The
     * <i>value</i> field, a double, is returned unchanged.  To check
     * whether this is a good thing to do, call <i>isNumber()</i>.
     * @return a <tt>double</tt> primitive type.
     */
    public double asDouble() {
        return value;
    }

    /**
     * Return the current value as a <tt>long</tt>.
     * The <tt>double</tt> <i>value</i> is converted to an <tt>long</tt> and
     * returned.  No other checking or conversion occurs.
     */
     public long asLong() {
         return (long)value;
     }

    /**
     * Return the current value as an <tt>int</tt>.
     * The <tt>double</tt> <i>value</i> is converted to an <tt>int</tt> and
     * returned.  No other checking or conversion occurs.
     */
     public int asInt() {
         return (int)value;
     }

    /**
     * Subtract the operand from the current value.
     * @param op The value to subtract.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException If the unit power of the operands is different
     */
    public Numeric subtract(Numeric op) throws PropertyException {
        // Check of same dimension
        if (power != op.power)
            throw new PropertyException
                    ("Can't subtract Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't subtract Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        
        // Subtract each type of value
        value -= op.value;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Add the operand to the current value.
     * @param op The value to add.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException
     * if the unit power of the operands is different.
     */
    public Numeric add(Numeric op) throws PropertyException {
        // Check of same powerension
        if (power != op.power)
            throw new PropertyException
                    ("Can't add Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't add Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }

        // Add each type of value
        value += op.value;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Derive the remainder from a truncating division (mod).  As with
     * additive operators, the values must be absolute <tt>Numeric</tt>s
     * of the same unit value. (5.9.6)
     * @param op a <tt>Numeric</tt> representing the divisor
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException If the unit power of the operands is
     * different or if the operands have different baseunits.
     */
    public Numeric mod(Numeric op) throws PropertyException {
        if (power != op.power)
            throw new PropertyException
                    ("Can't mod Numerics of different unit powers.");
        if (baseunit != op.baseunit) {
             throw new PropertyException
                     ("Can't mod Numerics of different baseunits: "
                      + getBaseunitString() + " " + op.getBaseunitString());
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't mod relative lengths."
                      + getBaseunitString() + " " + op.getBaseunitString());
        }

        value %= op.value;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Derive the remainder from a truncating division (mod).  As with
     * additive operators, the values must be absolute <tt>Numeric</tt>s
     * of the same unit value. (5.9.6)
     * In this case, the argument is a <tt>double</tt>, i.e., an absolute
     * Numeric with a unit value of zero.
     * <p> Originally the restriction for this method was lifted as noted
     * here.  There is no indication of why.  The restriction is now in place. 
     * <p>In this case only, the restriction
     * on the same unit power is lifted.
     * @param op a <tt>double</tt> containing the divisor
     * @return <i>Numeric</i>; this object.
     */
    public Numeric mod(double op) throws PropertyException {
        if (power != 0)
            throw new PropertyException
                    ("Can't mod Numerics of different unit powers.");
        if (baseunit != NUMBER) {
             throw new PropertyException
                     ("Can't mod Numerics of different baseunits: "
                      + getBaseunitString() + " literal double");
        }
        if ((baseunit & REL_LENGTH) != 0) {
            throw new PropertyException
                    ("Can't mod relative lengths."
                      + getBaseunitString());
        }

        value %= op;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Multiply the the current value by the operand.
     * @param op The multiplier.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException for invalid combinations.
     */
    public Numeric multiply(Numeric op) throws PropertyException {
        if (baseunit == NUMBER) {
            // NUMBER is the universal multiplier
            // Multiply and convert to the basetype and power of the arg
            value *= op.value;
            power = op.power;
            baseunit = op.baseunit;
        } else { // this is not a NUMBER - must be unit or relative length
            if (op.baseunit == NUMBER) {
                // NUMBER is the universal multiplier
                value *= op.value;
            } else { // op not a NUMBER - must be UNIT or REL_LENGTH
                if ((baseunit & UNIT ) != 0) { // this is a unit
                    if ((op.baseunit & UNIT) != 0) { // op is a unit
                        if (baseunit != op.baseunit) { // not same unit
                            throw new PropertyException
                                    ("Can't multiply Numerics of different "
                                     + "baseunits: " + getBaseunitString()
                                     + " " + op.getBaseunitString());
                        } else { // same unit- multiply OK
                            value *= op.value;
                            power += op.power;
                        }
                    } else { // op is a REL_LENGTH; this is a UNIT
                        if (baseunit == MILLIPOINTS) { // this is a LENGTH
                            // Result is a length * numeric relative factor
                            value *= op.value;
                        } else { // this is a UNIT not a LENGTH
                            throw new PropertyException
                                    ("Can't multiply a unit other than a "
                                     + "length by a relative length: "
                                     + getBaseunitString()
                                     + " " + op.getBaseunitString());
                        }
                    }
                } else { // this is a REL_LENGTH  op is UNIT or REL_LENGTH
                    // only valid if op is a LENGTH
                    if (op.baseunit == MILLIPOINTS) {
                        value *= op.value;
                        power = op.power;
                        baseunit = op.baseunit;
                    } else { // Can't be done
                        throw new PropertyException
                                ("Can't multiply a unit other than a "
                                 + "length by a relative length: "
                                 + getBaseunitString()
                                 + " " + op.getBaseunitString());
                    }
                }
            }
        } // end of this == unit or relative length
        // Perfom some validity checks
        if (isNumeric() && power != 0)
            throw new PropertyException
                    ("Number, Ems or Percentage with non-zero power");
        // if the operation has resulted in a non-NUMERIC reducing to
        // a unit power of 0, change the type to NUMBER
        if (power == 0 && ! this.isNumeric()) baseunit = NUMBER;
        // Always normalize if we are dealing with degrees.
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Multiply the the current value by the <tt>double</tt> operand.
     * @param op The multiplier.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric multiply(double op) {
        value *= op;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Divide the the current value by the operand.
     * @param op the divisor.
     * @return <i>Numeric</i>; this object.
     * @throws PropertyException for invalid combinations.
     */
    public Numeric divide(Numeric op) throws PropertyException {
        if (baseunit == NUMBER) {
            // NUMBER is the universal multiplier
            // Divide and convert to the basetype and power of the arg
            value /= op.value;
            power = op.power;
            baseunit = op.baseunit;
        } else { // this is not a NUMBER - must be unit or relative length
            if (op.baseunit == NUMBER) {
                // NUMBER is the universal multiplier
                value /= op.value;
            } else { // op not a NUMBER - must be UNIT or REL_LENGTH
                if ((baseunit & UNIT ) != 0) { // this is a unit
                    if ((op.baseunit & UNIT) != 0) { // op is a unit
                        if (baseunit != op.baseunit) { // not same unit
                            throw new PropertyException
                                    ("Can't divide Numerics of different "
                                     + "baseunits: " + getBaseunitString()
                                     + " " + op.getBaseunitString());
                        } else { // same unit- divide OK
                            value /= op.value;
                            power -= op.power;
                        }
                    } else { // op is a REL_LENGTH; this is a UNIT
                        if (baseunit == MILLIPOINTS) { // this is a LENGTH
                            // Result is a length * numeric relative factor
                            value /= op.value;
                        } else { // this is a UNIT not a LENGTH
                            throw new PropertyException
                                    ("Can't multiply a unit other than a "
                                     + "length by a relative length: "
                                     + getBaseunitString()
                                     + " " + op.getBaseunitString());
                        }
                    }
                } else { // this is a REL_LENGTH  op is UNIT or REL_LENGTH
                    // only valid if op is a LENGTH
                    if (op.baseunit == MILLIPOINTS) {
                        value /= op.value;
                        power = op.power;
                        baseunit = op.baseunit;
                    } else { // Can't be done
                        throw new PropertyException
                                ("Can't multiply a unit other than a "
                                 + "length by a relative length: "
                                 + getBaseunitString()
                                 + " " + op.getBaseunitString());
                    }
                }
            }
        } // end of this == unit or relative length
        // Perfom some validity checks
        if (isNumeric() && power != 0)
            throw new PropertyException
                    ("Number, Ems or Percentage with non-zero power");
        // if the operation has resulted in a non-NUMERIC reducing to
        // a unit power of 0, change the type to NUMBER
        if (power == 0 && ! this.isNumeric()) baseunit = NUMBER;
        // Always normalize if we are dealing with degrees.
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Divide the the current value by the <tt>double</tt> operand.
     * @param op The divisor.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric divide(double op) {
        value /= op;
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Negate the value of the property.
     * @return <i>Numeric</i>; this object.
     */
    public Numeric negate() {
        value = -value;
        // I think this is OK
        if (baseunit == DEGREES) this.value = Angle.normalize(this);
        return this;
    }

    /**
     * Return the absolute value of this <tt>Numeric</tt>.  This is an
     * implementation of the core function library <tt>abs</tt> function.
     * It is only valid on an absolute numeric of unit power zero.
     * @return A <tt>double</tt> containing the absolute value.
     * @exception PropertyException if <i>value</i> is not unit power zero.
     */
    public double abs() throws PropertyException {
        if (power != 0)
            throw new PropertyException
                    ("abs requires absolute numeric of unit power zero");
        return Math.abs(value);
    }

    /**
     * Return a <tt>double</tt> which is the maximum of the current value and
     * the operand.  This is an implementation of the core function library
     * <tt>max</tt> function.  It is only valid for comparison of two
     * absolute <tt>Numeric</tt> values.
     * @param op a <tt>Numeric</tt> representing the comparison value.
     * @return a <tt>double</tt> representing the <i>max</i> of
     * <i>this.value</i> and the <i>value</i> of <i>op</i>.
     * @throws PropertyException If the power of this
     * object and the operand are different or not 0.
     */
    public double max(Numeric op) throws PropertyException {
        // Only compare if both have unit power 0
        if (power == op.power && power == 0) {
            return Math.max(value, op.value); 
        }
        throw new PropertyException
                ("max() must compare numerics of unit power 0.");
    }

    /**
     * Return a <tt>double</tt> which is the minimum of the current value and
     * the operand.  This is an implementation of the core function library
     * <tt>min</tt> function.  It is only valid for comparison of two
     * absolute <tt>Numeric</tt> values.
     * @param op a <tt>Numeric</tt> representing the comparison value.
     * @return a <tt>double</tt> representing the <i>min</i> of
     * <i>this.value</i> and the <i>value</i> of <i>op</i>.
     * @throws PropertyException If the power of this
     * object and the operand are different or not 0.
     */
    public double min(Numeric op) throws PropertyException {
        // Only compare if both have unit power 0
        if (power == op.power && power == 0) {
            return Math.min(value, op.value); 
        }
        throw new PropertyException
                ("min() must compare numerics of unit power 0.");
    }

    /**
     * Return a <tt>double</tt> which is the ceiling of the current value.
     * This is an implementation of the core function library
     * <tt>ceiling</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>double</tt> representing the <i>ceiling</i> value.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public double ceiling() throws PropertyException {

        if (power == 0) {
            return Math.ceil(value); 
        }
        throw new PropertyException
                ("ceiling() requires unit power 0.");
    }

    /**
     * Return a <tt>double</tt> which is the floor of the current value.
     * This is an implementation of the core function library
     * <tt>floor</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>double</tt> representing the <i>floor</i> value.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public double floor() throws PropertyException {

        if (power == 0) {
            return Math.floor(value); 
        }
        throw new PropertyException
                ("floor() requires unit power 0.");
    }

    /**
     * Return a <tt>long</tt> which is the rounded current value.
     * This is an implementation of the core function library
     * <tt>round</tt> function.  It is only valid for an absolute
     * numeric value of unit power 0.
     * @return a <tt>long</tt> representing the <i>round</i>ed value.
     * Note that although the method returns a <tt>long</tt>,
     * the XSL funtion is expressed in terms of a <i>numeric</i>.
     * @throws PropertyException If the unit power of the
     * object is not 0.
     */
    public long round() throws PropertyException {

        if (power == 0) {
            return Math.round(value); 
        }
        throw new PropertyException
                ("round() requires unit power 0.");
    }

    /**
     * @param baseunit an <tt>init</tt> encoding the base unit.
     * @return a String containing the text name of the <i>baseunit</i>
     * type or notification of an unrecognized baseunit/
     */
    public String getUnitTypeString(int baseunit) {
        switch (baseunit) {
        case NUMBER:
            return "numeric";
        case PERCENTAGE:
            return "percentage";
        case MILLIPOINTS:
            return "millipoints";
        case HERTZ:
            return "Hertz";
        case MILLISECS:
            return "milliseconds";
        case DEGREES:
            return "degrees";
        default: 
            return "Unrecognized baseunit type: " + baseunit;
        }
        
    }

    /**
     * @return a String containing the text name of the current <i>baseunit</i>
     * type or notification of an unrecognized baseunit/
     */
    public String getBaseunitString() {
        return getUnitTypeString(baseunit);
    }

    /**
     * @return a String containing the text name of the original
     * <i>baseunit</i> type or notification of an unrecognized baseunit/
     */
    public String getOriginalBaseunitString() {
        return getUnitTypeString(originalBaseUnit);
    }

    /**
     * @return a String containing the text name of the original
     * <i>unit</i> type or notification of an unrecognized unit.
     * Defined relative to the <i>originalBaseUnit</i>.
     */
    public String getOriginalUnitString() {
        switch (originalBaseUnit) {
        case NUMBER:
            return "";
        case PERCENTAGE:
            return "%";
        case MILLIPOINTS:
            return Length.getUnitName(originalUnit);
        case HERTZ:
            return Frequency.getUnitName(originalUnit);
        case MILLISECS:
            return Time.getUnitName(originalUnit);
        case DEGREES:
            return Angle.getUnitName(originalUnit);
        default: 
            return "Unrecognized original baseunit type: " + originalBaseUnit;
        }
        
    }

    /**
     * @param unit an <tt>int</tt> encoding a unit.
     * @return the <tt>String</tt> name of the unit.
     */
    public static String getUnitName(int unit) {
        switch (unit) {
        case NUMBER:
            return "";
        case PERCENTAGE:
            return "%";
        default:
            return "";
        }
    }

    public String toString() {
        return "" + value + getBaseunitString()
                + (power != 0 ? "^" + power : "")
                + "\n" + super.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
