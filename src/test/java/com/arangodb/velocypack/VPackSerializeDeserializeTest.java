/*
 * DISCLAIMER
 *
 * Copyright 2016 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.velocypack;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.arangodb.velocypack.VPack.SerializeOptions;
import com.arangodb.velocypack.annotations.Expose;
import com.arangodb.velocypack.annotations.SerializedName;
import com.arangodb.velocypack.exception.VPackException;

/**
 * @author Mark Vollmary
 *
 */
public class VPackSerializeDeserializeTest {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");// ISO 8601
	static {
		DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	protected static class TestEntityBoolean {
		private boolean a = true;
		private boolean b = false;
		private Boolean c = Boolean.TRUE;
		private Boolean d = Boolean.FALSE;

		public boolean isA() {
			return a;
		}

		public void setA(final boolean a) {
			this.a = a;
		}

		public boolean isB() {
			return b;
		}

		public void setB(final boolean b) {
			this.b = b;
		}

		public Boolean getC() {
			return c;
		}

		public void setC(final Boolean c) {
			this.c = c;
		}

		public Boolean getD() {
			return d;
		}

		public void setD(final Boolean d) {
			this.d = d;
		}
	}

	@Test
	public void fromBoolean() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityBoolean());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a = vpack.get("a");
			assertThat(a.isBoolean(), is(true));
			assertThat(a.getAsBoolean(), is(true));
		}
		{
			final VPackSlice b = vpack.get("b");
			assertThat(b.isBoolean(), is(true));
			assertThat(b.getAsBoolean(), is(false));
		}
		{
			final VPackSlice c = vpack.get("c");
			assertThat(c.isBoolean(), is(true));
			assertThat(c.getAsBoolean(), is(true));
		}
		{
			final VPackSlice d = vpack.get("d");
			assertThat(d.isBoolean(), is(true));
			assertThat(d.getAsBoolean(), is(false));
		}
	}

	@Test
	public void toBoolean() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("a", false);
			builder.add("b", true);
			builder.add("c", Boolean.FALSE);
			builder.add("d", Boolean.TRUE);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityBoolean entity = new VPack.Builder().build().deserialize(vpack, TestEntityBoolean.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a, is(false));
		assertThat(entity.b, is(true));
		assertThat(entity.c, is(Boolean.FALSE));
		assertThat(entity.d, is(Boolean.TRUE));
	}

	protected static class TestEntityString {
		private String s = "test";
		private Character c1 = 't';
		private char c2 = 't';

		public String getS() {
			return s;
		}

		public void setS(final String s) {
			this.s = s;
		}

		public Character getC1() {
			return c1;
		}

		public void setC1(final Character c1) {
			this.c1 = c1;
		}

		public char getC2() {
			return c2;
		}

		public void setC2(final char c2) {
			this.c2 = c2;
		}
	}

	@Test
	public void fromStrings() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityString());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice s = vpack.get("s");
			assertThat(s.isString(), is(true));
			assertThat(s.getAsString(), is("test"));
		}
		{
			final VPackSlice c1 = vpack.get("c1");
			assertThat(c1.isString(), is(true));
			assertThat(c1.getAsChar(), is('t'));
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			assertThat(c2.isString(), is(true));
			assertThat(c2.getAsChar(), is('t'));
		}
	}

	@Test
	public void toStrings() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("s", "abc");
			builder.add("c1", 'd');
			builder.add("c2", 'd');
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityString entity = new VPack.Builder().build().deserialize(vpack, TestEntityString.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s, is("abc"));
		assertThat(entity.c1, is(new Character('d')));
		assertThat(entity.c1, is(new Character('d')));
	}

	protected static class TestEntityInteger {
		private int i1 = 1;
		private Integer i2 = 1;

		public int getI1() {
			return i1;
		}

		public void setI1(final int i1) {
			this.i1 = i1;
		}

		public Integer getI2() {
			return i2;
		}

		public void setI2(final Integer i2) {
			this.i2 = i2;
		}
	}

	@Test
	public void fromInteger() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityInteger());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice i1 = vpack.get("i1");
			assertThat(i1.isInteger(), is(true));
			assertThat(i1.getAsInt(), is(1));
		}
		{
			final VPackSlice i2 = vpack.get("i2");
			assertThat(i2.isInteger(), is(true));
			assertThat(i2.getAsInt(), is(1));
		}
	}

	@Test
	public void fromNegativeInteger() throws VPackException {
		final TestEntityInteger entity = new TestEntityInteger();
		entity.i1 = -50;
		entity.i2 = -50;
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice i1 = vpack.get("i1");
			assertThat(i1.isInteger(), is(true));
			assertThat(i1.getAsInt(), is(-50));
		}
		{
			final VPackSlice i2 = vpack.get("i2");
			assertThat(i2.isInteger(), is(true));
			assertThat(i2.getAsInt(), is(-50));
		}
	}

	@Test
	public void toInteger() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("i1", 2);
			builder.add("i2", 3);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityInteger entity = new VPack.Builder().build().deserialize(vpack, TestEntityInteger.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.i1, is(2));
		assertThat(entity.i2, is(new Integer(3)));
	}

	@Test
	public void toNegativeInteger() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("i1", -50);
			builder.add("i2", -50);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityInteger entity = new VPack.Builder().build().deserialize(vpack, TestEntityInteger.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.i1, is(-50));
		assertThat(entity.i2, is(new Integer(-50)));
	}

	protected static class TestEntityLong {
		private long l1 = 1;
		private Long l2 = 1L;

		public long getL1() {
			return l1;
		}

		public void setL1(final long l1) {
			this.l1 = l1;
		}

		public Long getL2() {
			return l2;
		}

		public void setL2(final Long l2) {
			this.l2 = l2;
		}
	}

	@Test
	public void fromLong() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityLong());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice l1 = vpack.get("l1");
			assertThat(l1.isInteger(), is(true));
			assertThat(l1.getAsLong(), is(1L));
		}
		{
			final VPackSlice l2 = vpack.get("l2");
			assertThat(l2.isInteger(), is(true));
			assertThat(l2.getAsLong(), is(1L));
		}
	}

	@Test
	public void fromNegativeLong() throws VPackException {
		final TestEntityLong entity = new TestEntityLong();
		entity.l1 = -100L;
		entity.l2 = new Long(-300);
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice l1 = vpack.get("l1");
			assertThat(l1.isInteger(), is(true));
			assertThat(l1.getAsLong(), is(-100L));
		}
		{
			final VPackSlice l2 = vpack.get("l2");
			assertThat(l2.isInteger(), is(true));
			assertThat(l2.getAsLong(), is(-300L));
		}
	}

	@Test
	public void toLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("l1", 2);
			builder.add("l2", 3);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack.Builder().build().deserialize(vpack, TestEntityLong.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.l1, is(2L));
		assertThat(entity.l2, is(new Long(3)));
	}

	@Test
	public void toNegativeLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("l1", -100L);
			builder.add("l2", -300L);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack.Builder().build().deserialize(vpack, TestEntityLong.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.l1, is(-100L));
		assertThat(entity.l2, is(new Long(-300)));
	}

	@Test
	public void negativeLong() throws VPackException {
		final TestEntityLong entity = new TestEntityLong();
		entity.l1 = -100L;
		entity.l2 = new Long(-300);
		final VPack vp = new VPack.Builder().build();
		final TestEntityLong out = vp.deserialize(vp.serialize(entity), TestEntityLong.class);
		assertThat(out.l1, is(entity.l1));
		assertThat(out.l2, is(entity.l2));
	}

	@Test
	public void intToLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("l1", 100);
			builder.add("l2", 300);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack.Builder().build().deserialize(vpack, TestEntityLong.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.l1, is(100L));
		assertThat(entity.l2, is(new Long(300)));
	}

	@Test
	public void negativeIntToLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("l1", -100);
			builder.add("l2", -300);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack.Builder().build().deserialize(vpack, TestEntityLong.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.l1, is(-100L));
		assertThat(entity.l2, is(new Long(-300)));
	}

	@Test
	public void negativeLongToInt() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("i1", -100L);
			builder.add("i2", -300L);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityInteger entity = new VPack.Builder().build().deserialize(vpack, TestEntityInteger.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.i1, is(-100));
		assertThat(entity.i2, is(new Integer(-300)));
	}

	@Test
	public void negativeLongToShort() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("s1", -100L);
			builder.add("s2", -300L);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityShort entity = new VPack.Builder().build().deserialize(vpack, TestEntityShort.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s1, is((short) -100));
		assertThat(entity.s2, is(new Short((short) -300)));
	}

	@Test
	public void negativeShortToLong() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("l1", (short) -100);
			builder.add("l2", (short) -300);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityLong entity = new VPack.Builder().build().deserialize(vpack, TestEntityLong.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.l1, is(-100L));
		assertThat(entity.l2, is(new Long(-300)));
	}

	protected static class TestEntityFloat {
		private float f1 = 1;
		private Float f2 = 1F;

		public float getF1() {
			return f1;
		}

		public void setF1(final float f1) {
			this.f1 = f1;
		}

		public Float getF2() {
			return f2;
		}

		public void setF2(final Float f2) {
			this.f2 = f2;
		}
	}

	@Test
	public void fromFloat() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityFloat());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice f1 = vpack.get("f1");
			assertThat(f1.isDouble(), is(true));
			assertThat(f1.getAsFloat(), is(1.0F));
		}
		{
			final VPackSlice f2 = vpack.get("f2");
			assertThat(f2.isDouble(), is(true));
			assertThat(f2.getAsFloat(), is(1.0F));
		}
	}

	@Test
	public void toFloat() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("f1", 2F);
			builder.add("f2", 3F);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityFloat entity = new VPack.Builder().build().deserialize(vpack, TestEntityFloat.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.f1, is(2F));
		assertThat(entity.f2, is(new Float(3)));
	}

	protected static class TestEntityShort {
		private short s1 = 1;
		private Short s2 = 1;

		public short getS1() {
			return s1;
		}

		public void setS1(final short s1) {
			this.s1 = s1;
		}

		public Short getS2() {
			return s2;
		}

		public void setS2(final Short s2) {
			this.s2 = s2;
		}
	}

	@Test
	public void fromShort() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityShort());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice s1 = vpack.get("s1");
			assertThat(s1.isInteger(), is(true));
			assertThat(s1.getAsShort(), is((short) 1));
		}
		{
			final VPackSlice s2 = vpack.get("s2");
			assertThat(s2.isInteger(), is(true));
			assertThat(s2.getAsShort(), is((short) 1));
		}
	}

	@Test
	public void toShort() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("s1", 2);
			builder.add("s2", 3);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityShort entity = new VPack.Builder().build().deserialize(vpack, TestEntityShort.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s1, is((short) 2));
		assertThat(entity.s2, is(new Short((short) 3)));
	}

	protected static class TestEntityByte {
		private byte b1 = 1; // short integer path
		private Byte b2 = 100; // integer path

		public byte getB1() {
			return b1;
		}

		public void setB1(final byte b1) {
			this.b1 = b1;
		}

		public Byte getB2() {
			return b2;
		}

		public void setB2(final Byte b2) {
			this.b2 = b2;
		}
	}

	@Test
	public void fromByte() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityByte());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice b1 = vpack.get("b1");
			assertThat(b1.isInteger(), is(true));
			assertThat(b1.getAsByte(), is((byte) 1));
		}
		{
			final VPackSlice b2 = vpack.get("b2");
			assertThat(b2.isInteger(), is(true));
			assertThat(b2.getAsByte(), is((byte) 100));
		}
	}

	@Test
	public void toByte() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("b1", 30); // integer path
			builder.add("b2", 4); // short integer path
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityByte entity = new VPack.Builder().build().deserialize(vpack, TestEntityByte.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.b1, is((byte) 30));
		assertThat(entity.b2, is(new Byte((byte) 4)));
	}

	protected static class TestEntityDouble {
		private Double d1 = 1.5;
		private double d2 = 1.5;

		public Double getD1() {
			return d1;
		}

		public void setD1(final Double d1) {
			this.d1 = d1;
		}

		public double getD2() {
			return d2;
		}

		public void setD2(final double d2) {
			this.d2 = d2;
		}
	}

	@Test
	public void fromDouble() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityDouble());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice d1 = vpack.get("d1");
			assertThat(d1.isDouble(), is(true));
			assertThat(d1.getAsDouble(), is(1.5));
		}
		{
			final VPackSlice d2 = vpack.get("d2");
			assertThat(d2.isDouble(), is(true));
			assertThat(d2.getAsDouble(), is(1.5));
		}
	}

	@Test
	public void toDouble() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("d1", 2.25);
			builder.add("d2", 3.75);
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityDouble entity = new VPack.Builder().build().deserialize(vpack, TestEntityDouble.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.d1, is(2.25));
		assertThat(entity.d2, is(3.75));
	}

	protected static class TestEntityBigNumber {
		private BigInteger bi = BigInteger.valueOf(1L);
		private BigDecimal bd = BigDecimal.valueOf(1.5);

		public BigInteger getBi() {
			return bi;
		}

		public void setBi(final BigInteger bi) {
			this.bi = bi;
		}

		public BigDecimal getBd() {
			return bd;
		}

		public void setBd(final BigDecimal bd) {
			this.bd = bd;
		}
	}

	@Test
	public void fromBigNumbers() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityBigNumber());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice bi = vpack.get("bi");
			assertThat(bi.isString(), is(true));
			assertThat(bi.getAsBigInteger(), is(BigInteger.valueOf(1L)));
		}
		{
			final VPackSlice bd = vpack.get("bd");
			assertThat(bd.isString(), is(true));
			assertThat(bd.getAsBigDecimal(), is(BigDecimal.valueOf(1.5)));
		}
	}

	@Test
	public void toBigNumbers() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("bi", BigInteger.valueOf(2));
			builder.add("bd", BigDecimal.valueOf(3.75));
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityBigNumber entity = new VPack.Builder().build().deserialize(vpack, TestEntityBigNumber.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.bi, is(BigInteger.valueOf(2)));
		assertThat(entity.bd, is(BigDecimal.valueOf(3.75)));
	}

	@Test
	public void bigDecimal() {
		final BigDecimal fromDouble = BigDecimal.valueOf(-710.01);
		final BigDecimal fromString = new BigDecimal("-710.01");
		assertThat(fromDouble, is(fromString));
		assertThat(new VPackBuilder().add(fromDouble).slice().getAsBigDecimal(), is(fromDouble));
		assertThat(new VPackBuilder().add(fromString).slice().getAsBigDecimal(), is(fromDouble));
	}

	protected static class TestEntityArray {
		private String[] a1 = { "a", "b", "cd" };
		private int[] a2 = { 1, 2, 3, 4, 5 };
		private boolean[] a3 = { true, true, false };
		private TestEnum[] a4 = TestEnum.values();

		public String[] getA1() {
			return a1;
		}

		public void setA1(final String[] a1) {
			this.a1 = a1;
		}

		public int[] getA2() {
			return a2;
		}

		public void setA2(final int[] a2) {
			this.a2 = a2;
		}

		public boolean[] getA3() {
			return a3;
		}

		public void setA3(final boolean[] a3) {
			this.a3 = a3;
		}

		public TestEnum[] getA4() {
			return a4;
		}

		public void setA4(final TestEnum[] a4) {
			this.a4 = a4;
		}

	}

	@Test
	public void fromArray() throws VPackException {
		final TestEntityArray entity = new TestEntityArray();
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a1 = vpack.get("a1");
			assertThat(a1.isArray(), is(true));
			assertThat(a1.getLength(), is(entity.a1.length));
			for (int i = 0; i < a1.getLength(); i++) {
				assertThat(a1.get(i).getAsString(), is(entity.a1[i]));
			}
		}
		{
			final VPackSlice a2 = vpack.get("a2");
			assertThat(a2.isArray(), is(true));
			assertThat(a2.getLength(), is(entity.a2.length));
			for (int i = 0; i < a2.getLength(); i++) {
				assertThat(a2.get(i).getAsInt(), is(entity.a2[i]));
			}
		}
		{
			final VPackSlice a3 = vpack.get("a3");
			assertThat(a3.isArray(), is(true));
			assertThat(a3.getLength(), is(entity.a3.length));
			for (int i = 0; i < a3.getLength(); i++) {
				assertThat(a3.get(i).getAsBoolean(), is(entity.a3[i]));
			}
		}
		{
			final VPackSlice a4 = vpack.get("a4");
			assertThat(a4.isArray(), is(true));
			assertThat(a4.getLength(), is(entity.a4.length));
			for (int i = 0; i < a4.getLength(); i++) {
				assertThat(TestEnum.valueOf(a4.get(i).getAsString()), is(entity.a4[i]));
			}
		}
	}

	@Test
	public void toArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("a1", ValueType.ARRAY);
				builder.add("a");
				builder.add("b");
				builder.add("c");
				builder.close();
			}
			{
				builder.add("a2", ValueType.ARRAY);
				builder.add(1);
				builder.add(2);
				builder.add(3);
				builder.add(4);
				builder.close();
			}
			{
				builder.add("a3", ValueType.ARRAY);
				builder.add(false);
				builder.add(true);
				builder.close();
			}
			{
				builder.add("a4", ValueType.ARRAY);
				builder.add(TestEnum.A.name());
				builder.add(TestEnum.B.name());
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArray entity = new VPack.Builder().build().deserialize(vpack, TestEntityArray.class);
		assertThat(entity, is(notNullValue()));
		{
			assertThat(entity.a1.length, is(3));
			assertThat(entity.a1[0], is("a"));
			assertThat(entity.a1[1], is("b"));
			assertThat(entity.a1[2], is("c"));
		}
		{
			assertThat(entity.a2.length, is(4));
			assertThat(entity.a2[0], is(1));
			assertThat(entity.a2[1], is(2));
			assertThat(entity.a2[2], is(3));
			assertThat(entity.a2[3], is(4));
		}
		{
			assertThat(entity.a3.length, is(2));
			assertThat(entity.a3[0], is(false));
			assertThat(entity.a3[1], is(true));
		}
		{
			assertThat(entity.a4.length, is(2));
			assertThat(entity.a4[0], is(TestEnum.A));
			assertThat(entity.a4[1], is(TestEnum.B));
		}
	}

	@Test
	public void fromArrayWithNull() {
		final TestEntityArray entity = new TestEntityArray();
		entity.a1 = new String[] { "foo", null };

		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));

		final VPackSlice a1 = vpack.get("a1");
		assertThat(a1.isArray(), is(true));
		assertThat(a1.size(), is(2));
		assertThat(a1.get(0).isString(), is(true));
		assertThat(a1.get(0).getAsString(), is("foo"));
		assertThat(a1.get(1).isNull(), is(true));
	}

	protected enum TestEnum {
		A, B, C
	}

	protected static class TestEntityEnum {
		private TestEnum e1 = TestEnum.A;

		public TestEnum getE1() {
			return e1;
		}

		public void setE1(final TestEnum e1) {
			this.e1 = e1;
		}
	}

	@Test
	public void fromEnum() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityEnum());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice e1 = vpack.get("e1");
			assertThat(e1.isString(), is(true));
			assertThat(TestEnum.valueOf(e1.getAsString()), is(TestEnum.A));
		}
	}

	@Test
	public void toEnum() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("e1", TestEnum.B.name());
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityEnum entity = new VPack.Builder().build().deserialize(vpack, TestEntityEnum.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.e1, is(TestEnum.B));
	}

	protected static class TestEntityObject {
		private TestEntityLong o1 = new TestEntityLong();
		private TestEntityArray o2 = new TestEntityArray();

		public TestEntityLong getO1() {
			return o1;
		}

		public void setO1(final TestEntityLong o1) {
			this.o1 = o1;
		}

		public TestEntityArray getO2() {
			return o2;
		}

		public void setO2(final TestEntityArray o2) {
			this.o2 = o2;
		}
	}

	@Test
	public void fromObject() throws VPackException {
		final TestEntityObject entity = new TestEntityObject();
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice o1 = vpack.get("o1");
			assertThat(o1.isObject(), is(true));
			{
				final VPackSlice l1 = o1.get("l1");
				assertThat(l1.isInteger(), is(true));
				assertThat(l1.getAsLong(), is(1L));
			}
			{
				final VPackSlice l2 = o1.get("l2");
				assertThat(l2.isInteger(), is(true));
				assertThat(l2.getAsLong(), is(1L));
			}
		}
		{
			final VPackSlice o2 = vpack.get("o2");
			assertThat(o2.isObject(), is(true));
			{
				final VPackSlice a1 = o2.get("a1");
				assertThat(a1.isArray(), is(true));
				assertThat(a1.getLength(), is(entity.o2.a1.length));
				for (int i = 0; i < a1.getLength(); i++) {
					assertThat(a1.get(i).getAsString(), is(entity.o2.a1[i]));
				}
			}
			{
				final VPackSlice a2 = o2.get("a2");
				assertThat(a2.isArray(), is(true));
				assertThat(a2.getLength(), is(entity.o2.a2.length));
				for (int i = 0; i < a2.getLength(); i++) {
					assertThat(a2.get(i).getAsInt(), is(entity.o2.a2[i]));
				}
			}
			{
				final VPackSlice a3 = o2.get("a3");
				assertThat(a3.isArray(), is(true));
				assertThat(a3.getLength(), is(entity.o2.a3.length));
				for (int i = 0; i < a3.getLength(); i++) {
					assertThat(a3.get(i).getAsBoolean(), is(entity.o2.a3[i]));
				}
			}
			{
				final VPackSlice a4 = o2.get("a4");
				assertThat(a4.isArray(), is(true));
				assertThat(a4.getLength(), is(entity.o2.a4.length));
				for (int i = 0; i < a4.getLength(); i++) {
					assertThat(TestEnum.valueOf(a4.get(i).getAsString()), is(entity.o2.a4[i]));
				}
			}
		}
	}

	@Test
	public void toObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("o1", ValueType.OBJECT);
				builder.add("l1", 5L);
				builder.add("l2", 5L);
				builder.close();
			}
			{
				builder.add("o2", ValueType.OBJECT);
				{
					builder.add("a1", ValueType.ARRAY);
					builder.add("a");
					builder.add("b");
					builder.add("c");
					builder.close();
				}
				{
					builder.add("a2", ValueType.ARRAY);
					builder.add(1);
					builder.add(2);
					builder.add(3);
					builder.add(4);
					builder.close();
				}
				{
					builder.add("a3", ValueType.ARRAY);
					builder.add(false);
					builder.add(true);
					builder.close();
				}
				{
					builder.add("a4", ValueType.ARRAY);
					builder.add(TestEnum.A.name());
					builder.add(TestEnum.B.name());
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityObject entity = new VPack.Builder().build().deserialize(vpack, TestEntityObject.class);
		assertThat(entity, is(notNullValue()));
		{
			assertThat(entity.o1.l1, is(5L));
			assertThat(entity.o1.l2, is(new Long(5)));
		}
		{
			assertThat(entity.o2.a1.length, is(3));
			assertThat(entity.o2.a1[0], is("a"));
			assertThat(entity.o2.a1[1], is("b"));
			assertThat(entity.o2.a1[2], is("c"));
		}
		{
			assertThat(entity.o2.a2.length, is(4));
			assertThat(entity.o2.a2[0], is(1));
			assertThat(entity.o2.a2[1], is(2));
			assertThat(entity.o2.a2[2], is(3));
			assertThat(entity.o2.a2[3], is(4));
		}
		{
			assertThat(entity.o2.a3.length, is(2));
			assertThat(entity.o2.a3[0], is(false));
			assertThat(entity.o2.a3[1], is(true));
		}
		{
			assertThat(entity.o2.a4.length, is(2));
			assertThat(entity.o2.a4[0], is(TestEnum.A));
			assertThat(entity.o2.a4[1], is(TestEnum.B));
		}
	}

	protected static class TestEntityArrayInArray {
		private long[][] a1;

		public long[][] getA1() {
			return a1;
		}

		public void setA1(final long[][] a1) {
			this.a1 = a1;
		}
	}

	@Test
	public void fromArrayInArray() throws VPackException {
		final TestEntityArrayInArray entity = new TestEntityArrayInArray();
		entity.a1 = new long[][] { { 1, 2, 3 }, { 4, 5, 6 } };
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a1 = vpack.get("a1");
			assertThat(a1.isArray(), is(true));
			assertThat(a1.getLength(), is(entity.a1.length));
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				assertThat(at.isArray(), is(true));
				assertThat(at.getLength(), is(entity.a1[i].length));
				for (int j = 0; j < at.getLength(); j++) {
					final VPackSlice atat = at.get(j);
					assertThat(atat.isInteger(), is(true));
					assertThat(atat.getAsLong(), is(entity.a1[i][j]));
				}
			}
		}
	}

	@Test
	public void toArrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("a1", ValueType.ARRAY);
				{
					builder.add(ValueType.ARRAY);
					builder.add(1);
					builder.add(2);
					builder.add(3);
					builder.close();
				}
				{
					builder.add(ValueType.ARRAY);
					builder.add(4);
					builder.add(5);
					builder.add(6);
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArrayInArray entity = new VPack.Builder().build().deserialize(vpack,
			TestEntityArrayInArray.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a1.length, is(2));
		{
			assertThat(entity.a1[0].length, is(3));
			assertThat(entity.a1[0][0], is(1L));
			assertThat(entity.a1[0][1], is(2L));
			assertThat(entity.a1[0][2], is(3L));
		}
		{
			assertThat(entity.a1[1].length, is(3));
			assertThat(entity.a1[1][0], is(4L));
			assertThat(entity.a1[1][1], is(5L));
			assertThat(entity.a1[1][2], is(6L));
		}
	}

	@SuppressWarnings("serial")
	protected static class TestCollection extends LinkedList<String> {

	}

	protected static class TestEntityCollectionExtendedWithNulls {

		protected TestCollection a1;

		public TestCollection getA1() {
			return a1;
		}

		public void setA1(final TestCollection a1) {
			this.a1 = a1;
		}

	}

	@Test
	public void fromCollectionExtendedWithNulls() throws Exception {

		final TestCollection collection = new TestCollection();
		collection.add("one");
		collection.add(null);
		collection.add("two");

		final TestEntityCollectionExtendedWithNulls entity = new TestEntityCollectionExtendedWithNulls();
		entity.setA1(collection);

		final VPackSlice vpack = new VPack.Builder().serializeNullValues(true).build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a1 = vpack.get("a1");
			assertThat(a1.isArray(), is(true));
			assertThat(a1.getLength(), is(entity.a1.size()));

			VPackSlice at = a1.get(0);
			assertThat(at.isString(), is(true));
			assertThat(at.getAsString(), is(entity.a1.get(0)));
			at = a1.get(1);
			assertThat(at.isNull(), is(true));
			at = a1.get(2);
			assertThat(at.isString(), is(true));
			assertThat(at.getAsString(), is(entity.a1.get(2)));
		}
	}

	@Test
	public void toCollectionExtendedWithNulls() throws Exception {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("a1", ValueType.ARRAY);
				builder.add("one");
				builder.add(ValueType.NULL);
				builder.add("two");
				builder.close();
			}
			builder.close();
		}

		final VPackSlice vpack = builder.slice();
		final TestEntityCollectionExtendedWithNulls entity = new VPack.Builder().build().deserialize(vpack,
			TestEntityCollectionExtendedWithNulls.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.getA1(), is(notNullValue()));
		assertThat(entity.getA1().size(), is(3));
		assertThat(entity.getA1(), contains("one", null, "two"));
	}

	protected static class TestEntityArrayInArrayInArray {

		private double[][][] a1;

		public double[][][] getA1() {
			return a1;
		}

		public void setA1(final double[][][] a1) {
			this.a1 = a1;
		}

	}

	@Test
	public void fromArrayInArrayInArray() throws VPackException {
		final TestEntityArrayInArrayInArray entity = new TestEntityArrayInArrayInArray();
		entity.setA1(new double[][][] { { { 1.5, 2.25 }, { 10.5, 20.25 } }, { { 100.5 }, { 200.25 } } });
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a1 = vpack.get("a1");
			assertThat(a1.isArray(), is(true));
			assertThat(a1.getLength(), is(entity.a1.length));
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				assertThat(at.isArray(), is(true));
				assertThat(at.getLength(), is(entity.a1[i].length));
				for (int j = 0; j < at.getLength(); j++) {
					final VPackSlice atat = at.get(j);
					assertThat(atat.isArray(), is(true));
					assertThat(atat.getLength(), is(entity.a1[i][j].length));
					for (int k = 0; k < atat.getLength(); k++) {
						final VPackSlice atatat = atat.get(k);
						assertThat(atatat.isDouble(), is(true));
						assertThat(atatat.getAsDouble(), is(entity.a1[i][j][k]));
					}
				}
			}
		}
	}

	@Test
	public void toArrayInArrayInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("a1", ValueType.ARRAY);
				builder.add(ValueType.ARRAY);
				{
					builder.add(ValueType.ARRAY);
					builder.add(1.5);
					builder.add(2.5);
					builder.add(3.5);
					builder.close();
				}
				{
					builder.add(ValueType.ARRAY);
					builder.add(4.5);
					builder.add(5.5);
					builder.add(6.5);
					builder.close();
				}
				{
					builder.add(ValueType.ARRAY);
					builder.add(7.5);
					builder.add(8.5);
					builder.add(9.5);
					builder.close();
				}
				builder.close();
				builder.add(ValueType.ARRAY);
				{
					builder.add(ValueType.ARRAY);
					builder.add(1.5);
					builder.add(2.5);
					builder.add(3.5);
					builder.close();
				}
				{
					builder.add(ValueType.ARRAY);
					builder.add(4.5);
					builder.add(5.5);
					builder.add(6.5);
					builder.close();
				}
				{
					builder.add(ValueType.ARRAY);
					builder.add(7.5);
					builder.add(8.5);
					builder.add(9.5);
					builder.close();
				}
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityArrayInArrayInArray entity = new VPack.Builder().build().deserialize(vpack,
			TestEntityArrayInArrayInArray.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a1.length, is(2));
		{
			assertThat(entity.a1[0].length, is(3));
			assertThat(entity.a1[0][0].length, is(3));
			assertThat(entity.a1[0][0][0], is(1.5));
			assertThat(entity.a1[0][0][1], is(2.5));
			assertThat(entity.a1[0][0][2], is(3.5));
			assertThat(entity.a1[0][1].length, is(3));
			assertThat(entity.a1[0][1][0], is(4.5));
			assertThat(entity.a1[0][1][1], is(5.5));
			assertThat(entity.a1[0][1][2], is(6.5));
			assertThat(entity.a1[0][2].length, is(3));
			assertThat(entity.a1[0][2][0], is(7.5));
			assertThat(entity.a1[0][2][1], is(8.5));
			assertThat(entity.a1[0][2][2], is(9.5));
		}
		{
			assertThat(entity.a1[1].length, is(3));
			assertThat(entity.a1[1][0].length, is(3));
			assertThat(entity.a1[1][0][0], is(1.5));
			assertThat(entity.a1[1][0][1], is(2.5));
			assertThat(entity.a1[1][0][2], is(3.5));
			assertThat(entity.a1[1][1].length, is(3));
			assertThat(entity.a1[1][1][0], is(4.5));
			assertThat(entity.a1[1][1][1], is(5.5));
			assertThat(entity.a1[1][1][2], is(6.5));
			assertThat(entity.a1[1][2].length, is(3));
			assertThat(entity.a1[1][2][0], is(7.5));
			assertThat(entity.a1[1][2][1], is(8.5));
			assertThat(entity.a1[1][2][2], is(9.5));
		}
	}

	protected static class TestEntityObjectInArray {
		private TestEntityString[] a1;

		public TestEntityString[] getA1() {
			return a1;
		}

		public void setA1(final TestEntityString[] a1) {
			this.a1 = a1;
		}
	}

	@Test
	public void fromObjectInArray() throws VPackException {
		final TestEntityObjectInArray entity = new TestEntityObjectInArray();
		{
			final TestEntityString[] a1 = new TestEntityString[2];
			final TestEntityString s = new TestEntityString();
			s.setS("abc");
			a1[0] = s;
			a1[1] = s;
			entity.setA1(a1);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice a1 = vpack.get("a1");
			assertThat(a1.isArray(), is(true));
			assertThat(a1.getLength(), is(2));
			for (int i = 0; i < a1.getLength(); i++) {
				final VPackSlice at = a1.get(i);
				assertThat(at.isObject(), is(true));
				final VPackSlice s = at.get("s");
				assertThat(s.isString(), is(true));
				assertThat(s.getAsString(), is("abc"));
			}
		}
	}

	@Test
	public void toObjectInArray() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("a1", ValueType.ARRAY);
			{
				builder.add(ValueType.OBJECT);
				builder.add("s", "abc");
				builder.close();
			}
			builder.close();
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityObjectInArray entity = new VPack.Builder().build().deserialize(vpack,
			TestEntityObjectInArray.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a1, is(notNullValue()));
		assertThat(entity.a1.length, is(1));
		final TestEntityString st = entity.a1[0];
		assertThat(st, is(notNullValue()));
		assertThat(st.s, is("abc"));
	}

	protected static class TestEntityA {
		private String a = "a";

		public String getA() {
			return a;
		}

		public void setA(final String a) {
			this.a = a;
		}
	}

	protected static class TestEntityB extends TestEntityA {
		private String b = "b";

		public String getB() {
			return b;
		}

		public void setB(final String b) {
			this.b = b;
		}
	}

	@Test
	public void fromInheritance() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityB());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(2));
		{
			final VPackSlice a = vpack.get("a");
			assertThat(a.isString(), is(true));
			assertThat(a.getAsString(), is("a"));
		}
		{
			final VPackSlice b = vpack.get("b");
			assertThat(b.isString(), is(true));
			assertThat(b.getAsString(), is("b"));
		}
	}

	@Test
	public void toInheritance() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("a", "test");
			builder.add("b", "test");
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		{
			final TestEntityA entity = new VPack.Builder().build().deserialize(vpack, TestEntityA.class);
			assertThat(entity, is(notNullValue()));
			assertThat(entity.getA(), is("test"));
		}
		{
			final TestEntityB entity = new VPack.Builder().build().deserialize(vpack, TestEntityB.class);
			assertThat(entity, is(notNullValue()));
			assertThat(entity.getA(), is("test"));
			assertThat(entity.getB(), is("test"));
		}
	}

	protected static class TestEntityC {
		private TestEntityD d;

		public TestEntityD getD() {
			return d;
		}

		public void setD(final TestEntityD d) {
			this.d = d;
		}
	}

	protected static interface TestEntityD {
		String getD();

		void setD(String d);
	}

	protected static class TestEntityDImpl implements TestEntityD {
		private String d = "d";

		@Override
		public String getD() {
			return d;
		}

		@Override
		public void setD(final String d) {
			this.d = d;
		}
	}

	@Test
	public void fromInterface() throws VPackException {
		final TestEntityC entity = new TestEntityC();
		entity.setD(new TestEntityDImpl());
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice d = vpack.get("d");
			assertThat(d.isObject(), is(true));
			final VPackSlice dd = d.get("d");
			assertThat(dd.isString(), is(true));
			assertThat(dd.getAsString(), is("d"));
		}
	}

	@Test
	public void toInterface() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("d", ValueType.OBJECT);
			builder.add("d", "test");
			builder.close();
			builder.close();
		}
		final VPackSlice slice = builder.slice();
		final VPack vPack = new VPack.Builder()
				.registerInstanceCreator(TestEntityD.class, new VPackInstanceCreator<TestEntityD>() {
					@Override
					public TestEntityD createInstance() {
						return new TestEntityDImpl();
					}
				}).build();
		final TestEntityC entity = vPack.deserialize(slice, TestEntityC.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.d, is(notNullValue()));
		assertThat(entity.d.getD(), is("test"));
	}

	protected static class TestEntityCollection {
		private Collection<String> c1 = new LinkedList<String>();
		private List<String> c2 = new ArrayList<String>();
		private ArrayList<String> c3 = new ArrayList<String>();
		private Set<String> c4 = new LinkedHashSet<String>();
		private HashSet<String> c5 = new HashSet<String>();

		public TestEntityCollection() {
			super();
		}

		public Collection<String> getC1() {
			return c1;
		}

		public void setC1(final Collection<String> c1) {
			this.c1 = c1;
		}

		public List<String> getC2() {
			return c2;
		}

		public void setC2(final List<String> c2) {
			this.c2 = c2;
		}

		public ArrayList<String> getC3() {
			return c3;
		}

		public void setC3(final ArrayList<String> c3) {
			this.c3 = c3;
		}

		public Set<String> getC4() {
			return c4;
		}

		public void setC4(final Set<String> c4) {
			this.c4 = c4;
		}

		public HashSet<String> getC5() {
			return c5;
		}

		public void setC5(final HashSet<String> c5) {
			this.c5 = c5;
		}
	}

	@Test
	public void fromCollection() throws VPackException {
		final TestEntityCollection entity = new TestEntityCollection();
		{
			entity.c1.add("test");
			entity.c2.add("test");
			entity.c3.add("test");
			entity.c4.add("test");
			entity.c5.add("test");
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice c1 = vpack.get("c1");
			assertThat(c1.isArray(), is(true));
			assertThat(c1.getLength(), is(1));
			assertThat(c1.get(0).getAsString(), is("test"));
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			assertThat(c2.isArray(), is(true));
			assertThat(c2.getLength(), is(1));
			assertThat(c2.get(0).getAsString(), is("test"));
		}
		{
			final VPackSlice c3 = vpack.get("c3");
			assertThat(c3.isArray(), is(true));
			assertThat(c3.getLength(), is(1));
			assertThat(c3.get(0).getAsString(), is("test"));
		}
		{
			final VPackSlice c4 = vpack.get("c4");
			assertThat(c4.isArray(), is(true));
			assertThat(c4.getLength(), is(1));
			assertThat(c4.get(0).getAsString(), is("test"));
		}
		{
			final VPackSlice c5 = vpack.get("c5");
			assertThat(c5.isArray(), is(true));
			assertThat(c5.getLength(), is(1));
			assertThat(c5.get(0).getAsString(), is("test"));
		}
	}

	@Test
	public void toCollection() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("c1", ValueType.ARRAY);
				builder.add("test1");
				builder.add("test2");
				builder.close();
			}
			{
				builder.add("c2", ValueType.ARRAY);
				builder.add("test1");
				builder.add("test2");
				builder.close();
			}
			{
				builder.add("c3", ValueType.ARRAY);
				builder.add("test1");
				builder.add("test2");
				builder.close();
			}
			{
				builder.add("c4", ValueType.ARRAY);
				builder.add("test1");
				builder.add("test2");
				builder.close();
			}
			{
				builder.add("c5", ValueType.ARRAY);
				builder.add("test1");
				builder.add("test2");
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityCollection entity = new VPack.Builder().build().deserialize(vpack, TestEntityCollection.class);
		assertThat(entity, is(notNullValue()));
		{
			checkCollection(entity.c1);
			checkCollection(entity.c2);
			checkCollection(entity.c3);
			checkCollection(entity.c4);
			checkCollection(entity.c5);
		}
	}

	private void checkCollection(final Collection<String> col) {
		assertThat(col, is(notNullValue()));
		assertThat(col.size(), is(2));
		for (final Iterator<String> iterator = col.iterator(); iterator.hasNext();) {
			final String next = iterator.next();
			assertThat("test1".equals(next) || "test2".equals(next), is(true));
		}
	}

	protected static class TestEntityCollectionWithObjects {
		private Collection<TestEntityString> c1;
		private Set<TestEntityArray> c2;

		public Collection<TestEntityString> getC1() {
			return c1;
		}

		public void setC1(final Collection<TestEntityString> c1) {
			this.c1 = c1;
		}

		public Set<TestEntityArray> getC2() {
			return c2;
		}

		public void setC2(final Set<TestEntityArray> c2) {
			this.c2 = c2;
		}
	}

	@Test
	public void fromCollectionWithObjects() throws VPackException {
		final TestEntityCollectionWithObjects entity = new TestEntityCollectionWithObjects();
		{
			final Collection<TestEntityString> c1 = new ArrayList<VPackSerializeDeserializeTest.TestEntityString>();
			c1.add(new TestEntityString());
			c1.add(new TestEntityString());
			entity.setC1(c1);
			final Set<TestEntityArray> c2 = new HashSet<VPackSerializeDeserializeTest.TestEntityArray>();
			c2.add(new TestEntityArray());
			entity.setC2(c2);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice c1 = vpack.get("c1");
			assertThat(c1.isArray(), is(true));
			assertThat(c1.getLength(), is(2));
			assertThat(c1.get(0).isObject(), is(true));
			assertThat(c1.get(1).isObject(), is(true));
			{
				final VPackSlice s = c1.get(0).get("s");
				assertThat(s.isString(), is(true));
				assertThat(s.getAsString(), is("test"));
			}
		}
		{
			final VPackSlice c2 = vpack.get("c2");
			assertThat(c2.isArray(), is(true));
			assertThat(c2.getLength(), is(1));
			assertThat(c2.get(0).isObject(), is(true));
			{
				final VPackSlice a2 = c2.get(0).get("a2");
				assertThat(a2.isArray(), is(true));
				assertThat(a2.getLength(), is(5));
				for (int i = 0; i < a2.getLength(); i++) {
					final VPackSlice at = a2.get(i);
					assertThat(at.isInteger(), is(true));
					assertThat(at.getAsInt(), is(i + 1));
				}
			}
		}
	}

	@Test
	public void toCollectionWithObjects() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("c1", ValueType.ARRAY);
				builder.add(ValueType.OBJECT);
				builder.add("s", "abc");
				builder.close();
				builder.close();
			}
			{
				builder.add("c2", ValueType.ARRAY);
				builder.add(ValueType.OBJECT);
				builder.add("a2", ValueType.ARRAY);
				for (int i = 0; i < 10; i++) {
					builder.add(i);
				}
				builder.close();
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityCollectionWithObjects entity = new VPack.Builder().build().deserialize(vpack,
			TestEntityCollectionWithObjects.class);
		assertThat(entity, is(notNullValue()));
		{
			assertThat(entity.c1, is(notNullValue()));
			assertThat(entity.c1.size(), is(1));
			assertThat(entity.c1.iterator().next().s, is("abc"));
		}
		{
			assertThat(entity.c2, is(notNullValue()));
			assertThat(entity.c2.size(), is(1));
			final int[] array = entity.c2.iterator().next().a2;
			for (int i = 0; i < array.length; i++) {
				assertThat(array[i], is(i));
			}
		}
	}

	protected static class TestEntityMap {
		private Map<String, String> m1;
		private HashMap<Integer, String> m2;
		private Map<String, TestEntityString> m3;

		public Map<String, String> getM1() {
			return m1;
		}

		public void setM1(final Map<String, String> m1) {
			this.m1 = m1;
		}

		public HashMap<Integer, String> getM2() {
			return m2;
		}

		public void setM2(final HashMap<Integer, String> m2) {
			this.m2 = m2;
		}

		public Map<String, TestEntityString> getM3() {
			return m3;
		}

		public void setM3(final Map<String, TestEntityString> m3) {
			this.m3 = m3;
		}
	}

	@Test
	public void fromMap() throws VPackException {
		final TestEntityMap entity = new TestEntityMap();
		{
			final Map<String, String> m1 = new LinkedHashMap<String, String>();
			m1.put("a", "b");
			m1.put("c", "d");
			entity.setM1(m1);
			final HashMap<Integer, String> m2 = new HashMap<Integer, String>();
			m2.put(1, "a");
			m2.put(2, "b");
			entity.setM2(m2);
			final Map<String, TestEntityString> m3 = new HashMap<String, VPackSerializeDeserializeTest.TestEntityString>();
			final TestEntityString s = new TestEntityString();
			s.setS("abc");
			m3.put("a", s);
			entity.setM3(m3);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice m1 = vpack.get("m1");
			assertThat(m1.isObject(), is(true));
			assertThat(m1.getLength(), is(2));
			{
				final VPackSlice a = m1.get("a");
				assertThat(a.isString(), is(true));
				assertThat(a.getAsString(), is("b"));
			}
			{
				final VPackSlice c = m1.get("c");
				assertThat(c.isString(), is(true));
				assertThat(c.getAsString(), is("d"));
			}
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			assertThat(m2.isObject(), is(true));
			assertThat(m2.getLength(), is(2));
			{
				final VPackSlice one = m2.get("1");
				assertThat(one.isString(), is(true));
				assertThat(one.getAsString(), is("a"));
			}
			{
				final VPackSlice two = m2.get("2");
				assertThat(two.isString(), is(true));
				assertThat(two.getAsString(), is("b"));
			}
		}
		{
			final VPackSlice m3 = vpack.get("m3");
			assertThat(m3.isObject(), is(true));
			assertThat(m3.getLength(), is(1));
			final VPackSlice a = m3.get("a");
			assertThat(a.isObject(), is(true));
			final VPackSlice s = a.get("s");
			assertThat(s.isString(), is(true));
			assertThat(s.getAsString(), is("abc"));
		}
	}

	@Test
	public void toMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			{
				builder.add("m1", ValueType.OBJECT);
				builder.add("a", "a");
				builder.add("b", "b");
				builder.close();
			}
			{
				builder.add("m2", ValueType.OBJECT);
				builder.add("1", "a");
				builder.add("-1", "a");
				builder.close();
			}
			{
				builder.add("m3", ValueType.OBJECT);
				builder.add("a", ValueType.OBJECT);
				builder.add("s", "abc");
				builder.close();
				builder.close();
			}
			builder.close();
		}
		final VPackSlice vpack = builder.slice();
		final TestEntityMap entity = new VPack.Builder().build().deserialize(vpack, TestEntityMap.class);
		assertThat(entity, is(notNullValue()));
		{
			assertThat(entity.m1, is(notNullValue()));
			assertThat(entity.m1.size(), is(2));
			final String a = entity.m1.get("a");
			assertThat(a, is(notNullValue()));
			assertThat(a, is("a"));
			final String b = entity.m1.get("b");
			assertThat(b, is(notNullValue()));
			assertThat(b, is("b"));
		}
		{
			assertThat(entity.m2, is(notNullValue()));
			assertThat(entity.m2.size(), is(2));
			final String one = entity.m2.get(1);
			assertThat(one, is(notNullValue()));
			assertThat(one, is("a"));
			final String oneNegative = entity.m2.get(-1);
			assertThat(oneNegative, is(notNullValue()));
			assertThat(oneNegative, is("a"));
		}
		{
			assertThat(entity.m3, is(notNullValue()));
			assertThat(entity.m3.size(), is(1));
			final TestEntityString a = entity.m3.get("a");
			assertThat(a, is(notNullValue()));
			assertThat(a.s, is("abc"));
		}
	}

	protected static class TestEntityMapStringableKey {
		private Map<Boolean, String> m1;
		private Map<Integer, String> m2;
		private Map<Long, String> m3;
		private Map<Float, String> m4;
		private Map<Short, String> m5;
		private Map<Double, String> m6;
		private Map<Number, String> m7;
		private Map<BigInteger, String> m8;
		private Map<BigDecimal, String> m9;
		private Map<Character, String> m10;
		private Map<TestEnum, String> m11;

		public Map<Boolean, String> getM1() {
			return m1;
		}

		public void setM1(final Map<Boolean, String> m1) {
			this.m1 = m1;
		}

		public Map<Integer, String> getM2() {
			return m2;
		}

		public void setM2(final Map<Integer, String> m2) {
			this.m2 = m2;
		}

		public Map<Long, String> getM3() {
			return m3;
		}

		public void setM3(final Map<Long, String> m3) {
			this.m3 = m3;
		}

		public Map<Float, String> getM4() {
			return m4;
		}

		public void setM4(final Map<Float, String> m4) {
			this.m4 = m4;
		}

		public Map<Short, String> getM5() {
			return m5;
		}

		public void setM5(final Map<Short, String> m5) {
			this.m5 = m5;
		}

		public Map<Double, String> getM6() {
			return m6;
		}

		public void setM6(final Map<Double, String> m6) {
			this.m6 = m6;
		}

		public Map<Number, String> getM7() {
			return m7;
		}

		public void setM7(final Map<Number, String> m7) {
			this.m7 = m7;
		}

		public Map<BigInteger, String> getM8() {
			return m8;
		}

		public void setM8(final Map<BigInteger, String> m8) {
			this.m8 = m8;
		}

		public Map<BigDecimal, String> getM9() {
			return m9;
		}

		public void setM9(final Map<BigDecimal, String> m9) {
			this.m9 = m9;
		}

		public Map<Character, String> getM10() {
			return m10;
		}

		public void setM10(final Map<Character, String> m10) {
			this.m10 = m10;
		}

		public Map<TestEnum, String> getM11() {
			return m11;
		}

		public void setM11(final Map<TestEnum, String> m11) {
			this.m11 = m11;
		}

	}

	@Test
	public void fromMapStringableKey() throws VPackException {
		final TestEntityMapStringableKey entity = new TestEntityMapStringableKey();
		final String value = "test";
		{
			final Map<Boolean, String> m1 = new HashMap<Boolean, String>();
			m1.put(true, value);
			m1.put(false, value);
			entity.setM1(m1);
		}
		{
			final Map<Integer, String> m2 = new HashMap<Integer, String>();
			m2.put(1, value);
			m2.put(2, value);
			entity.setM2(m2);
		}
		{
			final Map<Long, String> m3 = new HashMap<Long, String>();
			m3.put(1L, value);
			m3.put(2L, value);
			entity.setM3(m3);
		}
		{
			final Map<Float, String> m4 = new HashMap<Float, String>();
			m4.put(1.5F, value);
			m4.put(2.25F, value);
			entity.setM4(m4);
		}
		{
			final Map<Short, String> m5 = new HashMap<Short, String>();
			m5.put(new Short("1"), value);
			m5.put(new Short("2"), value);
			entity.setM5(m5);
		}
		{
			final Map<Double, String> m6 = new HashMap<Double, String>();
			m6.put(1.5, value);
			m6.put(2.25, value);
			entity.setM6(m6);
		}
		{
			final Map<Number, String> m7 = new HashMap<Number, String>();
			m7.put(1.5, value);
			m7.put(1L, value);
			entity.setM7(m7);
		}
		{
			final Map<BigInteger, String> m8 = new HashMap<BigInteger, String>();
			m8.put(new BigInteger("1"), value);
			m8.put(new BigInteger("2"), value);
			entity.setM8(m8);
		}
		{
			final Map<BigDecimal, String> m9 = new HashMap<BigDecimal, String>();
			m9.put(new BigDecimal("1.5"), value);
			m9.put(new BigDecimal("2.25"), value);
			entity.setM9(m9);
		}
		{
			final Map<Character, String> m10 = new HashMap<Character, String>();
			m10.put('1', value);
			m10.put('a', value);
			entity.setM10(m10);
		}
		{
			final Map<TestEnum, String> m11 = new HashMap<VPackSerializeDeserializeTest.TestEnum, String>();
			m11.put(TestEnum.A, value);
			m11.put(TestEnum.B, value);
			entity.setM11(m11);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice m1 = vpack.get("m1");
			assertThat(m1.isObject(), is(true));
			assertThat(m1.getLength(), is(2));
			checkMapAttribute(m1.get("true"));
			checkMapAttribute(m1.get("false"));
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			assertThat(m2.isObject(), is(true));
			assertThat(m2.getLength(), is(2));
			checkMapAttribute(m2.get("1"));
			checkMapAttribute(m2.get("2"));
		}
		{
			final VPackSlice m3 = vpack.get("m3");
			assertThat(m3.isObject(), is(true));
			assertThat(m3.getLength(), is(2));
			checkMapAttribute(m3.get("1"));
			checkMapAttribute(m3.get("2"));
		}
		{
			final VPackSlice m4 = vpack.get("m4");
			assertThat(m4.isObject(), is(true));
			assertThat(m4.getLength(), is(2));
			checkMapAttribute(m4.get("1.5"));
			checkMapAttribute(m4.get("2.25"));
		}
		{
			final VPackSlice m5 = vpack.get("m5");
			assertThat(m5.isObject(), is(true));
			assertThat(m5.getLength(), is(2));
			checkMapAttribute(m5.get("1"));
			checkMapAttribute(m5.get("2"));
		}
		{
			final VPackSlice m6 = vpack.get("m6");
			assertThat(m6.isObject(), is(true));
			assertThat(m6.getLength(), is(2));
			checkMapAttribute(m6.get("1.5"));
			checkMapAttribute(m6.get("2.25"));
		}
		{
			final VPackSlice m7 = vpack.get("m7");
			assertThat(m7.isObject(), is(true));
			assertThat(m7.getLength(), is(2));
			checkMapAttribute(m7.get("1.5"));
			checkMapAttribute(m7.get("1"));
		}
		{
			final VPackSlice m8 = vpack.get("m8");
			assertThat(m8.isObject(), is(true));
			assertThat(m8.getLength(), is(2));
			checkMapAttribute(m8.get("1"));
			checkMapAttribute(m8.get("2"));
		}
		{
			final VPackSlice m9 = vpack.get("m9");
			assertThat(m9.isObject(), is(true));
			assertThat(m9.getLength(), is(2));
			checkMapAttribute(m9.get("1.5"));
			checkMapAttribute(m9.get("2.25"));
		}
		{
			final VPackSlice m10 = vpack.get("m10");
			assertThat(m10.isObject(), is(true));
			assertThat(m10.getLength(), is(2));
			checkMapAttribute(m10.get("1"));
			checkMapAttribute(m10.get("a"));
		}
		{
			final VPackSlice m11 = vpack.get("m11");
			assertThat(m11.isObject(), is(true));
			assertThat(m11.getLength(), is(2));
			checkMapAttribute(m11.get(TestEnum.A.name()));
			checkMapAttribute(m11.get(TestEnum.B.name()));
		}
	}

	@Test
	public void toMapSringableKey() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		{
			builder.add("m1", ValueType.OBJECT);
			builder.add("true", "test");
			builder.add("false", "test");
			builder.close();
		}
		{
			builder.add("m2", ValueType.OBJECT);
			builder.add("1", "test");
			builder.add("2", "test");
			builder.close();
		}
		{
			builder.add("m3", ValueType.OBJECT);
			builder.add("1", "test");
			builder.add("2", "test");
			builder.close();
		}
		{
			builder.add("m4", ValueType.OBJECT);
			builder.add("1.5", "test");
			builder.add("2.25", "test");
			builder.close();
		}
		{
			builder.add("m5", ValueType.OBJECT);
			builder.add("1", "test");
			builder.add("2", "test");
			builder.close();
		}
		{
			builder.add("m6", ValueType.OBJECT);
			builder.add("1.5", "test");
			builder.add("2.25", "test");
			builder.close();
		}
		{
			builder.add("m7", ValueType.OBJECT);
			builder.add("1.5", "test");
			builder.add("1", "test");
			builder.close();
		}
		{
			builder.add("m8", ValueType.OBJECT);
			builder.add("1", "test");
			builder.add("2", "test");
			builder.close();
		}
		{
			builder.add("m9", ValueType.OBJECT);
			builder.add("1.5", "test");
			builder.add("2.25", "test");
			builder.close();
		}
		{
			builder.add("m10", ValueType.OBJECT);
			builder.add("1", "test");
			builder.add("a", "test");
			builder.close();
		}
		{
			builder.add("m11", ValueType.OBJECT);
			builder.add(TestEnum.A.name(), "test");
			builder.add(TestEnum.B.name(), "test");
			builder.close();
		}
		builder.close();
		final TestEntityMapStringableKey entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityMapStringableKey.class);
		{
			assertThat(entity.m1.size(), is(2));
			checkMapAttribute(entity.m1.get(true));
			checkMapAttribute(entity.m1.get(false));
		}
		{
			assertThat(entity.m2.size(), is(2));
			checkMapAttribute(entity.m2.get(1));
			checkMapAttribute(entity.m2.get(2));
		}
		{
			assertThat(entity.m3.size(), is(2));
			checkMapAttribute(entity.m3.get(1L));
			checkMapAttribute(entity.m3.get(2L));
		}
		{
			assertThat(entity.m4.size(), is(2));
			checkMapAttribute(entity.m4.get(1.5F));
			checkMapAttribute(entity.m4.get(2.25F));
		}
		{
			assertThat(entity.m5.size(), is(2));
			checkMapAttribute(entity.m5.get(new Short("1")));
			checkMapAttribute(entity.m5.get(new Short("2")));
		}
		{
			assertThat(entity.m6.size(), is(2));
			checkMapAttribute(entity.m6.get(1.5));
			checkMapAttribute(entity.m6.get(2.25));
		}
		{
			assertThat(entity.m7.size(), is(2));
			checkMapAttribute(entity.m7.get(new Double(1.5)));
			checkMapAttribute(entity.m7.get(new Double(1L)));
		}
		{
			assertThat(entity.m8.size(), is(2));
			checkMapAttribute(entity.m8.get(new BigInteger("1")));
			checkMapAttribute(entity.m8.get(new BigInteger("2")));
		}
		{
			assertThat(entity.m9.size(), is(2));
			checkMapAttribute(entity.m9.get(new BigDecimal("1.5")));
			checkMapAttribute(entity.m9.get(new BigDecimal("2.25")));
		}
		{
			assertThat(entity.m10.size(), is(2));
			checkMapAttribute(entity.m10.get('1'));
			checkMapAttribute(entity.m10.get('a'));
		}
		{
			assertThat(entity.m11.size(), is(2));
			checkMapAttribute(entity.m11.get(TestEnum.A));
			checkMapAttribute(entity.m11.get(TestEnum.B));
		}
	}

	private void checkMapAttribute(final VPackSlice attr) {
		assertThat(attr.isString(), is(true));
		assertThat(attr.getAsString(), is("test"));
	}

	private void checkMapAttribute(final String attr) {
		assertThat(attr, is("test"));
	}

	protected static class TestEntityMapWithObjectKey {
		private Map<TestEntityLong, TestEntityCollection> m1;
		private Map<TestEntityLong, String> m2;

		public Map<TestEntityLong, TestEntityCollection> getM1() {
			return m1;
		}

		public void setM1(final Map<TestEntityLong, TestEntityCollection> m1) {
			this.m1 = m1;
		}

		public Map<TestEntityLong, String> getM2() {
			return m2;
		}

		public void setM2(final Map<TestEntityLong, String> m2) {
			this.m2 = m2;
		}
	}

	@Test
	public void fromMapWithObjectKey() throws VPackException {
		final TestEntityMapWithObjectKey entity = new TestEntityMapWithObjectKey();
		{
			final Map<TestEntityLong, TestEntityCollection> m1 = new HashMap<VPackSerializeDeserializeTest.TestEntityLong, VPackSerializeDeserializeTest.TestEntityCollection>();
			m1.put(new TestEntityLong(), new TestEntityCollection());
			m1.put(new TestEntityLong(), new TestEntityCollection());
			entity.setM1(m1);
		}
		{
			final Map<TestEntityLong, String> m2 = new HashMap<VPackSerializeDeserializeTest.TestEntityLong, String>();
			m2.put(new TestEntityLong(), "test");
			m2.put(new TestEntityLong(), "test");
			m2.put(new TestEntityLong(), "test");
			entity.setM2(m2);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice m1 = vpack.get("m1");
			assertThat(m1.isArray(), is(true));
			assertThat(m1.getLength(), is(2));
			for (int i = 0; i < m1.getLength(); i++) {
				final VPackSlice at = m1.get(i);
				assertThat(at.isObject(), is(true));
				assertThat(at.getLength(), is(2));
				{
					final VPackSlice key = at.get("key");
					assertThat(key.isObject(), is(true));
					final VPackSlice l1 = key.get("l1");
					assertThat(l1.isInteger(), is(true));
					assertThat(l1.getAsLong(), is(1L));
				}
				{
					final VPackSlice value = at.get("value");
					assertThat(value.isObject(), is(true));
					final VPackSlice c1 = value.get("c1");
					assertThat(c1.isArray(), is(true));
				}
			}
		}
		{
			final VPackSlice m2 = vpack.get("m2");
			assertThat(m2.isArray(), is(true));
			assertThat(m2.getLength(), is(3));
			for (int i = 0; i < m2.getLength(); i++) {
				final VPackSlice at = m2.get(i);
				assertThat(at.isObject(), is(true));
				assertThat(at.getLength(), is(2));
				{
					final VPackSlice key = at.get("key");
					assertThat(key.isObject(), is(true));
					final VPackSlice l1 = key.get("l1");
					assertThat(l1.isInteger(), is(true));
					assertThat(l1.getAsLong(), is(1L));
				}
				{
					final VPackSlice value = at.get("value");
					assertThat(value.isString(), is(true));
					assertThat(value.getAsString(), is("test"));
				}
			}
		}
	}

	@Test
	public void toMapWithObjectKey() throws VPackException {
		final int size = 2;
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		{
			builder.add("m1", ValueType.ARRAY);
			for (int i = 0; i < size; i++) {
				builder.add(ValueType.OBJECT);
				{
					builder.add("key", ValueType.OBJECT);
					builder.add("l1", 5L);
					builder.close();
				}
				{
					builder.add("value", ValueType.OBJECT);
					builder.add("c1", ValueType.ARRAY);
					builder.add("test");
					builder.close();
					builder.close();
				}
				builder.close();
			}
			builder.close();
		}
		{
			builder.add("m2", ValueType.ARRAY);
			for (int i = 0; i < size; i++) {
				builder.add(ValueType.OBJECT);
				{
					builder.add("key", ValueType.OBJECT);
					builder.add("l1", 5L);
					builder.close();
				}
				{
					builder.add("value", "test");
				}
				builder.close();
			}
			builder.close();
		}
		builder.close();
		final TestEntityMapWithObjectKey entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityMapWithObjectKey.class);
		assertThat(entity, is(notNullValue()));
		{
			assertThat(entity.m1, is(notNullValue()));
			assertThat(entity.m1.size(), is(size));
			for (final Entry<TestEntityLong, TestEntityCollection> entry : entity.m1.entrySet()) {
				assertThat(entry.getKey().l1, is(5L));
				assertThat(entry.getValue().c1.size(), is(1));
				assertThat(entry.getValue().c1.iterator().next(), is("test"));
			}
		}
		{
			assertThat(entity.m2, is(notNullValue()));
			assertThat(entity.m2.size(), is(2));
			for (final Entry<TestEntityLong, String> entry : entity.m2.entrySet()) {
				assertThat(entry.getKey().l1, is(5L));
				assertThat(entry.getValue(), is("test"));
			}
		}
	}

	protected static class TestEntityEmpty {

	}

	@Test
	public void fromEmptyObject() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityEmpty());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(0));
	}

	@Test
	public void toEmptyObject() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.close();
		final TestEntityEmpty entity = new VPack.Builder().build().deserialize(builder.slice(), TestEntityEmpty.class);
		assertThat(entity, is(notNullValue()));
	}

	protected static class TestEntityEmptyMap {
		private Map<String, Object> m;

		public Map<String, Object> getM() {
			return m;
		}

		public void setM(final Map<String, Object> m) {
			this.m = m;
		}
	}

	@Test
	public void fromEmptyMap() throws VPackException {
		final TestEntityEmptyMap entity = new TestEntityEmptyMap();
		entity.setM(new HashMap<String, Object>());
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(1));
		final VPackSlice m = vpack.get("m");
		assertThat(m.isObject(), is(true));
		assertThat(m.getLength(), is(0));
	}

	@Test
	public void toEmptyMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("m", ValueType.OBJECT);
		builder.close();
		builder.close();
		final TestEntityEmptyMap entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityEmptyMap.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.m, is(notNullValue()));
		assertThat(entity.m.size(), is(0));
	}

	protected static class TestEntityBaseAttributes {
		private String _key = "test1";
		private String _rev = "test2";
		private String _id = "test3";
		private String _from = "test4";
		private String _to = "test5";

		public String get_key() {
			return _key;
		}

		public void set_key(final String _key) {
			this._key = _key;
		}

		public String get_rev() {
			return _rev;
		}

		public void set_rev(final String _rev) {
			this._rev = _rev;
		}

		public String get_id() {
			return _id;
		}

		public void set_id(final String _id) {
			this._id = _id;
		}

		public String get_from() {
			return _from;
		}

		public void set_from(final String _from) {
			this._from = _from;
		}

		public String get_to() {
			return _to;
		}

		public void set_to(final String _to) {
			this._to = _to;
		}

	}

	@Test
	public void fromObjectWithAttributeAdapter() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityBaseAttributes());
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(5));
		{
			final VPackSlice key = vpack.get("_key");
			assertThat(key.isString(), is(true));
			assertThat(key.getAsString(), is("test1"));
		}
		{
			final VPackSlice rev = vpack.get("_rev");
			assertThat(rev.isString(), is(true));
			assertThat(rev.getAsString(), is("test2"));
		}
		{
			final VPackSlice id = vpack.get("_id");
			assertThat(id.isString(), is(true));
			assertThat(id.getAsString(), is("test3"));
		}
		{
			final VPackSlice from = vpack.get("_from");
			assertThat(from.isString(), is(true));
			assertThat(from.getAsString(), is("test4"));
		}
		{
			final VPackSlice to = vpack.get("_to");
			assertThat(to.isString(), is(true));
			assertThat(to.getAsString(), is("test5"));
		}
	}

	@Test
	public void toObjectWithAttributeAdapter() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("_key", "a");
			builder.add("_rev", "b");
			builder.add("_id", "c");
			builder.add("_from", "d");
			builder.add("_to", "e");
			builder.close();
		}
		final TestEntityBaseAttributes entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityBaseAttributes.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity._key, is("a"));
		assertThat(entity._rev, is("b"));
		assertThat(entity._id, is("c"));
		assertThat(entity._from, is("d"));
		assertThat(entity._to, is("e"));
	}

	@Test
	public void fromMapWithAttributeAdapter() throws VPackException {
		final TestEntityMap entity = new TestEntityMap();
		{
			final Map<String, String> m1 = new HashMap<String, String>();
			m1.put("_key", "test1");
			m1.put("_rev", "test2");
			m1.put("_id", "test3");
			m1.put("_from", "test4");
			m1.put("_to", "test5");
			entity.setM1(m1);
		}
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack.isObject(), is(true));
		final VPackSlice m1 = vpack.get("m1");
		assertThat(m1.isObject(), is(true));
		assertThat(m1.getLength(), is(5));
		{
			final VPackSlice key = m1.get("_key");
			assertThat(key.isString(), is(true));
			assertThat(key.getAsString(), is("test1"));
		}
		{
			final VPackSlice rev = m1.get("_rev");
			assertThat(rev.isString(), is(true));
			assertThat(rev.getAsString(), is("test2"));
		}
		{
			final VPackSlice id = m1.get("_id");
			assertThat(id.isString(), is(true));
			assertThat(id.getAsString(), is("test3"));
		}
		{
			final VPackSlice from = m1.get("_from");
			assertThat(from.isString(), is(true));
			assertThat(from.getAsString(), is("test4"));
		}
		{
			final VPackSlice to = m1.get("_to");
			assertThat(to.isString(), is(true));
			assertThat(to.getAsString(), is("test5"));
		}
	}

	@Test
	public void toMapWithAttributeAdapter() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		{
			builder.add(ValueType.OBJECT);
			builder.add("m1", ValueType.OBJECT);
			builder.add("_key", "a");
			builder.add("_rev", "b");
			builder.add("_id", "c");
			builder.add("_from", "d");
			builder.add("_to", "e");
			builder.close();
			builder.close();
		}
		final TestEntityMap entity = new VPack.Builder().build().deserialize(builder.slice(), TestEntityMap.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.m1, is(notNullValue()));
		assertThat(entity.m1.size(), is(5));
	}

	@Test
	public void customSerializer() throws VPackException {
		final String value = "abc";
		final VPack vp = new VPack.Builder()
				.registerSerializer(TestEntityString.class, new VPackSerializer<TestEntityString>() {
					@Override
					public void serialize(
						final VPackBuilder builder,
						final String attribute,
						final TestEntityString entity,
						final VPackSerializationContext context) throws VPackException {
						builder.add(attribute, ValueType.OBJECT);
						builder.add("not-s", entity.s);
						builder.close();
					}
				}).build();
		final TestEntityString entity = new TestEntityString();
		entity.setS(value);
		final VPackSlice vpack = vp.serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice s = vpack.get("not-s");
			assertThat(s.isString(), is(true));
			assertThat(s.getAsString(), is(value));
		}
	}

	@Test
	public void customDeserializer() throws VPackException {
		final String value = "abc";
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("not-s", value);
		builder.close();
		final VPack vp = new VPack.Builder()
				.registerDeserializer(TestEntityString.class, new VPackDeserializer<TestEntityString>() {
					@Override
					public TestEntityString deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						final TestEntityString entity = new TestEntityString();
						final VPackSlice nots = vpack.get("not-s");
						if (nots.isString()) {
							entity.s = nots.getAsString();
						}
						return entity;
					}
				}).build();
		final TestEntityString entity = vp.deserialize(builder.slice(), TestEntityString.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s, is(notNullValue()));
		assertThat(entity.s, is(value));
	}

	@Test
	public void customDeserializerByName() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("s", "test");
		builder.close();
		final TestEntityString entity = new VPack.Builder()
				.registerDeserializer("s", String.class, new VPackDeserializer<String>() {
					@Override
					public String deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						return vpack.getAsString() + "s";
					}

				}).build().deserialize(builder.slice(), TestEntityString.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s, is(notNullValue()));
		assertThat(entity.s, is("tests"));
	}

	@Test
	public void customDeserializerByNameWrongType() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("s", "test");
		builder.close();
		final TestEntityString entity = new VPack.Builder()
				.registerDeserializer("s", Integer.class, new VPackDeserializer<String>() {
					@Override
					public String deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						return "fail";
					}
				}).build().deserialize(builder.slice(), TestEntityString.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s, is(notNullValue()));
		assertThat(entity.s, is("test"));
	}

	@Test
	public void customSerializerWithContext() throws VPackException {
		final VPack vp = new VPack.Builder()
				.registerSerializer(TestEntityObject.class, new VPackSerializer<TestEntityObject>() {
					@Override
					public void serialize(
						final VPackBuilder builder,
						final String attribute,
						final TestEntityObject entity,
						final VPackSerializationContext context) throws VPackException {
						builder.add(attribute, ValueType.OBJECT);
						context.serialize(builder, "test", entity.o1);
						builder.close();
					}
				}).build();
		final TestEntityObject entity = new TestEntityObject();
		final VPackSlice vpack = vp.serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			final VPackSlice test = vpack.get("test");
			assertThat(test.isObject(), is(true));
			final VPackSlice l1 = test.get("l1");
			assertThat(l1.isInteger(), is(true));
			assertThat(l1.getAsInt(), is(1));
		}
	}

	@Test
	public void customDeserializerWithContext() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("test", ValueType.OBJECT);
		builder.add("l1", 5);
		builder.close();
		builder.close();
		final VPack vp = new VPack.Builder()
				.registerDeserializer(TestEntityObject.class, new VPackDeserializer<TestEntityObject>() {
					@Override
					public TestEntityObject deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						final TestEntityObject entity = new TestEntityObject();
						entity.o1 = context.deserialize(vpack.get("test"), TestEntityLong.class);
						return entity;
					}
				}).build();
		final TestEntityObject entity = vp.deserialize(builder.slice(), TestEntityObject.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.o1, is(notNullValue()));
		assertThat(entity.o1.l1, is(5L));
	}

	protected static class TestEntitySerializeAnnotation {

		@SerializedName("abc")
		private String test = "test";

		public String getTest() {
			return test;
		}

		public void setTest(final String test) {
			this.test = test;
		}

	}

	@Test
	public void fromSerializedName() throws VPackException {
		final TestEntitySerializeAnnotation entity = new TestEntitySerializeAnnotation();
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(1));
		final VPackSlice abc = vpack.get("abc");
		assertThat(abc.isString(), is(true));
		assertThat(abc.getAsString(), is("test"));
	}

	@Test
	public void toSerializedName() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("abc", "test2");
		builder.close();
		final TestEntitySerializeAnnotation entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntitySerializeAnnotation.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.test, is(notNullValue()));
		assertThat(entity.test, is("test2"));
	}

	protected static class TestEntityExpose {
		private String a;
		@Expose
		private String b;
		@Expose(serialize = true, deserialize = true)
		private String c;
		@Expose(serialize = true, deserialize = false)
		private String d;
		@Expose(serialize = false, deserialize = true)
		private String e;
		@Expose(serialize = false, deserialize = false)
		private String f;

		public String getA() {
			return a;
		}

		public void setA(final String a) {
			this.a = a;
		}

		public String getB() {
			return b;
		}

		public void setB(final String b) {
			this.b = b;
		}

		public String getC() {
			return c;
		}

		public void setC(final String c) {
			this.c = c;
		}

		public String getD() {
			return d;
		}

		public void setD(final String d) {
			this.d = d;
		}

		public String getE() {
			return e;
		}

		public void setE(final String e) {
			this.e = e;
		}

		public String getF() {
			return f;
		}

		public void setF(final String f) {
			this.f = f;
		}
	}

	@Test
	public void fromExpose() throws VPackException {
		final TestEntityExpose entity = new TestEntityExpose();
		entity.a = entity.b = entity.c = entity.d = entity.e = entity.f = "test";
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(4));
		{
			final VPackSlice a = vpack.get("a");
			assertThat(a.isString(), is(true));
			assertThat(a.getAsString(), is("test"));
		}
		{
			final VPackSlice b = vpack.get("b");
			assertThat(b.isString(), is(true));
			assertThat(b.getAsString(), is("test"));
		}
		{
			final VPackSlice c = vpack.get("c");
			assertThat(c.isString(), is(true));
			assertThat(c.getAsString(), is("test"));
		}
		{
			final VPackSlice d = vpack.get("d");
			assertThat(d.isString(), is(true));
			assertThat(d.getAsString(), is("test"));
		}
	}

	@Test
	public void toExpose() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", "test");
		builder.add("b", "test");
		builder.add("c", "test");
		builder.add("d", "test");
		builder.add("e", "test");
		builder.add("f", "test");
		builder.close();
		final TestEntityExpose entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityExpose.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a, is(notNullValue()));
		assertThat(entity.a, is("test"));
		assertThat(entity.b, is(notNullValue()));
		assertThat(entity.b, is("test"));
		assertThat(entity.c, is(notNullValue()));
		assertThat(entity.c, is("test"));
		assertThat(entity.e, is(notNullValue()));
		assertThat(entity.e, is("test"));
		assertThat(entity.d, is(nullValue()));
		assertThat(entity.f, is(nullValue()));
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private static @interface CustomFilterAnnotation {
		boolean serialize()

		default true;

		boolean deserialize() default true;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private static @interface CustomNamingAnnotation {
		String name();
	}

	private static class CustomAnEntity {
		@CustomFilterAnnotation(serialize = false)
		private String a = null;
		@CustomFilterAnnotation(deserialize = false)
		private String b = null;
		@CustomNamingAnnotation(name = "d")
		@CustomFilterAnnotation(deserialize = false)
		private String c = null;

		public CustomAnEntity() {
			super();
		}
	}

	@Test
	public void fromCutsomAnnotation() {
		final CustomAnEntity entity = new CustomAnEntity();
		entity.a = "1";
		entity.b = "2";
		entity.c = "3";
		final VPackSlice vpack = new VPack.Builder().annotationFieldFilter(CustomFilterAnnotation.class,
			new VPackAnnotationFieldFilter<CustomFilterAnnotation>() {

				@Override
				public boolean serialize(final CustomFilterAnnotation annotation) {
					return annotation.serialize();
				}

				@Override
				public boolean deserialize(final CustomFilterAnnotation annotation) {
					return annotation.deserialize();
				}
			}).annotationFieldNaming(CustomNamingAnnotation.class,
				new VPackAnnotationFieldNaming<CustomNamingAnnotation>() {
					@Override
					public String name(final CustomNamingAnnotation annotation) {
						return annotation.name();
					}
				}).build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get("a").isNone(), is(true));
		assertThat(vpack.get("b").isString(), is(true));
		assertThat(vpack.get("b").getAsString(), is("2"));
		assertThat(vpack.get("c").isNone(), is(true));
		assertThat(vpack.get("d").isString(), is(true));
		assertThat(vpack.get("d").getAsString(), is("3"));
	}

	@Test
	public void toCustomAnnotation() {
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add("a", "1").add("b", "2").add("c", "3")
				.add("d", "4").close().slice();

		final CustomAnEntity entity = new VPack.Builder().annotationFieldFilter(CustomFilterAnnotation.class,
			new VPackAnnotationFieldFilter<CustomFilterAnnotation>() {

				@Override
				public boolean serialize(final CustomFilterAnnotation annotation) {
					return annotation.serialize();
				}

				@Override
				public boolean deserialize(final CustomFilterAnnotation annotation) {
					return annotation.deserialize();
				}
			}).annotationFieldNaming(CustomNamingAnnotation.class,
				new VPackAnnotationFieldNaming<CustomNamingAnnotation>() {
					@Override
					public String name(final CustomNamingAnnotation annotation) {
						return annotation.name();
					}
				}).build().deserialize(vpack, CustomAnEntity.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a, is("1"));
		assertThat(entity.b, is(nullValue()));
		assertThat(entity.c, is(nullValue()));
	}

	@Test
	public void directFromCollection() throws VPackException {
		final Collection<String> list = new ArrayList<String>();
		list.add("test");
		final VPackSlice vpack = new VPack.Builder().build().serialize(list);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.size(), is(1));
		final VPackSlice test = vpack.get(0);
		assertThat(test.isString(), is(true));
		assertThat(test.getAsString(), is("test"));
	}

	@Test
	public void directFromCollectionWithType() throws VPackException {
		final Collection<TestEntityString> list = new ArrayList<VPackSerializeDeserializeTest.TestEntityString>();
		list.add(new TestEntityString());
		list.add(new TestEntityString());

		final VPackSlice vpack = new VPack.Builder().build().serialize(list,
			new SerializeOptions().type(new Type<Collection<TestEntityString>>() {
			}.getType()));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.getLength(), is(list.size()));
		for (int i = 0; i < list.size(); i++) {
			final VPackSlice entry = vpack.get(i);
			assertThat(entry.isObject(), is(true));
			assertThat(entry.getLength(), is(3));
			final VPackSlice s = entry.get("s");
			assertThat(s.isString(), is(true));
			assertThat(s.getAsString(), is("test"));
		}
	}

	@Test
	public void directToCollection() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.OBJECT);
		builder.add("s", "abc");
		builder.close();
		builder.close();
		final List<TestEntityString> list = new VPack.Builder().build().deserialize(builder.slice(),
			new Type<List<TestEntityString>>() {
			}.getType());
		assertThat(list, is(notNullValue()));
		assertThat(list.size(), is(1));
		final TestEntityString entry = list.get(0);
		assertThat(entry.s, is("abc"));
	}

	@Test
	public void directFromStringMap() throws VPackException {
		final Map<String, TestEntityString> map = new HashMap<String, VPackSerializeDeserializeTest.TestEntityString>();
		map.put("a", new TestEntityString());
		map.put("b", new TestEntityString());

		final VPackSlice vpack = new VPack.Builder().build().serialize(map,
			new SerializeOptions().type(new Type<Map<String, TestEntityString>>() {
			}.getType()));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(2));
		final VPackSlice a = vpack.get("a");
		checkStringEntity(a);
	}

	@Test
	public void directToStringMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.OBJECT);
		builder.add("s", "abc");
		builder.close();
		builder.close();
		final Map<String, TestEntityString> map = new VPack.Builder().build().deserialize(builder.slice(),
			new Type<Map<String, TestEntityString>>() {
			}.getType());
		assertThat(map, is(notNullValue()));
		assertThat(map.size(), is(1));
		final TestEntityString a = map.get("a");
		assertThat(a, is(notNullValue()));
		assertThat(a.s, is("abc"));
	}

	@Test
	public void directFromObjectMap() throws VPackException {
		final Map<TestEntityString, TestEntityString> map = new HashMap<VPackSerializeDeserializeTest.TestEntityString, VPackSerializeDeserializeTest.TestEntityString>();
		map.put(new TestEntityString(), new TestEntityString());
		map.put(new TestEntityString(), new TestEntityString());

		final VPackSlice vpack = new VPack.Builder().build().serialize(map,
			new SerializeOptions().type(new Type<Map<TestEntityString, TestEntityString>>() {
			}.getType()));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isArray(), is(true));
		assertThat(vpack.getLength(), is(map.size()));
		for (int i = 0; i < map.size(); i++) {
			final VPackSlice entry = vpack.get(i);
			final VPackSlice key = entry.get("key");
			checkStringEntity(key);
			final VPackSlice value = entry.get("value");
			checkStringEntity(value);
		}
	}

	@Test
	public void directFromMap() throws VPackException {
		final Map<String, Object> map = new HashMap<String, Object>();
		final TestEntityA entity = new TestEntityA();
		entity.a = "test";
		map.put("test", entity);
		final VPackSlice vpack = new VPack.Builder().build().serialize(map);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice test = vpack.get("test");
		assertThat(test.isObject(), is(true));
		final VPackSlice a = test.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("test"));
	}

	@Test
	public void directFromMapWithinMap() throws VPackException {
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("b", "test");
		map.put("a", map2);
		final VPackSlice vpack = new VPack.Builder().build().serialize(map);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(1));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isObject(), is(true));
		assertThat(a.size(), is(1));
		final VPackSlice b = a.get("b");
		assertThat(b.isString(), is(true));
		assertThat(b.getAsString(), is("test"));
	}

	private void checkStringEntity(final VPackSlice vpack) throws VPackException {
		final TestEntityString expected = new TestEntityString();
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.getLength(), is(3));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is(expected.s));
		final VPackSlice c1 = vpack.get("c1");
		assertThat(c1.isString(), is(true));
		assertThat(new Character(c1.getAsChar()), is(expected.c1));
		final VPackSlice c2 = vpack.get("c2");
		assertThat(c2.isString(), is(true));
		assertThat(c2.getAsChar(), is(expected.c2));
	}

	@Test
	public void directToObjectMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.ARRAY);
		builder.add(ValueType.OBJECT);
		builder.add("key", ValueType.OBJECT);
		builder.add("s", "abc");
		builder.close();
		builder.add("value", ValueType.OBJECT);
		builder.add("s", "abc");
		builder.close();
		builder.close();
		builder.close();
		final Map<TestEntityString, TestEntityString> map = new VPack.Builder().build().deserialize(builder.slice(),
			new Type<Map<TestEntityString, TestEntityString>>() {
			}.getType());
		assertThat(map, is(notNullValue()));
		assertThat(map.size(), is(1));
		for (final Entry<TestEntityString, TestEntityString> entry : map.entrySet()) {
			assertThat(entry.getKey().s, is("abc"));
			assertThat(entry.getValue().s, is("abc"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void directToMapWithinMap() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("a", ValueType.OBJECT);
		builder.add("b", "test");
		builder.add("c", true);
		builder.add("d", 1L);
		builder.add("e", 1.5);
		final Date date = new Date();
		builder.add("f", date);
		builder.add("g", ValueType.ARRAY);
		builder.close();
		builder.close();
		builder.close();
		final Map<String, Object> map = new VPack.Builder().build().deserialize(builder.slice(), Map.class);
		assertThat(map, is(notNullValue()));
		assertThat(map.size(), is(1));
		final Object a = map.get("a");
		assertThat(Map.class.isAssignableFrom(a.getClass()), is(true));
		final Map<String, Object> mapA = (Map<String, Object>) a;
		assertThat(mapA.size(), is(6));
		final Object b = mapA.get("b");
		assertThat(String.class.isAssignableFrom(b.getClass()), is(true));
		assertThat(b.toString(), is("test"));
		final Object c = mapA.get("c");
		assertThat(Boolean.class.isAssignableFrom(c.getClass()), is(true));
		assertThat(Boolean.class.cast(c), is(true));
		final Object d = mapA.get("d");
		assertThat(Number.class.isAssignableFrom(d.getClass()), is(true));
		assertThat(Number.class.cast(d).longValue(), is(1L));
		final Object e = mapA.get("e");
		assertThat(Double.class.isAssignableFrom(e.getClass()), is(true));
		assertThat(Double.class.cast(e), is(1.5));
		final Object f = mapA.get("f");
		assertThat(Date.class.isAssignableFrom(f.getClass()), is(true));
		assertThat(Date.class.cast(f), is(date));
		final Object g = mapA.get("g");
		assertThat(Collection.class.isAssignableFrom(g.getClass()), is(true));
		assertThat(List.class.isAssignableFrom(g.getClass()), is(true));
	}

	@Test
	public void dontSerializeNullValues() throws VPackException {
		final VPack serializer = new VPack.Builder().serializeNullValues(false).build();
		final TestEntityString entity = new TestEntityString();
		entity.setS(null);
		final VPackSlice vpack = serializer.serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isNone(), is(true));
	}

	@Test
	public void serializeNullValue() throws VPackException {
		final VPack serializer = new VPack.Builder().serializeNullValues(true).build();
		final TestEntityString entity = new TestEntityString();
		entity.setS(null);
		final VPackSlice vpack = serializer.serialize(entity);
		assertThat(vpack, is(notNullValue()));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isNull(), is(true));
	}

	@Test
	public void toNullValue() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("s", ValueType.NULL);
		builder.close();
		final TestEntityString entity = new VPack.Builder().build().deserialize(builder.slice(),
			TestEntityString.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.s, is(nullValue()));
		assertThat(entity.c1, is(notNullValue()));
		assertThat(entity.c2, is(notNullValue()));
	}

	@Test
	public void additionalFields() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<String, Object>();
		additionalFields.put("a", "test");
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity,
			new SerializeOptions().additionalFields(additionalFields));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.getLength(), is(4));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test"));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isString(), is(true));
		assertThat(a.getAsString(), is("test"));
	}

	@Test
	public void additionalFieldsNestedPojo() {
		final Map<String, Object> value = new HashMap<String, Object>();
		value.put("foo", "bar");
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityObject(),
			new SerializeOptions().additionalFields(value));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(3));
		assertThat(vpack.get("foo").isString(), is(true));
		assertThat(vpack.get("foo").getAsString(), is("bar"));

		assertThat(vpack.get("o1").isObject(), is(true));
		assertThat(vpack.get("o1").get("foo").isNone(), is(true));
		assertThat(vpack.get("o2").isObject(), is(true));
		assertThat(vpack.get("o2").get("foo").isNone(), is(true));
	}

	@Test
	public void additionalFieldsNestedMap() {
		final Map<String, Object> value = new HashMap<String, Object>();
		value.put("n", new HashMap<String, Object>());
		final Map<String, Object> additionalFields = new HashMap<String, Object>();
		additionalFields.put("foo", "bar");
		final VPackSlice vpack = new VPack.Builder().build().serialize(value,
			new SerializeOptions().additionalFields(additionalFields));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.size(), is(2));
		assertThat(vpack.get("foo").isString(), is(true));
		assertThat(vpack.get("foo").getAsString(), is("bar"));

		assertThat(vpack.get("n").isObject(), is(true));
		assertThat(vpack.get("n").size(), is(0));
	}

	@Test
	public void additionalDuplicatedFields() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<String, Object>();
		additionalFields.put("s", "test1");
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity,
			new SerializeOptions().additionalFields(additionalFields));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.getLength(), is(3));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test"));
	}

	@Test
	public void additionalNullFieldsExcludeNull() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<String, Object>();
		additionalFields.put("a", null);
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity,
			new SerializeOptions().additionalFields(additionalFields));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.getLength(), is(3));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test"));
	}

	@Test
	public void additionalNullFieldsIncludeNull() throws VPackException {
		final TestEntityString entity = new TestEntityString();
		final Map<String, Object> additionalFields = new HashMap<String, Object>();
		additionalFields.put("a", null);
		final VPack serializer = new VPack.Builder().serializeNullValues(true).build();
		final VPackSlice vpack = serializer.serialize(entity,
			new SerializeOptions().additionalFields(additionalFields));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.getLength(), is(4));
		final VPackSlice s = vpack.get("s");
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test"));
		final VPackSlice a = vpack.get("a");
		assertThat(a.isNull(), is(true));
	}

	@Test
	public void toSimpleString() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add("test");
		final String s = new VPack.Builder().build().deserialize(builder.slice(), String.class);
		assertThat(s, is(notNullValue()));
		assertThat(s, is("test"));
	}

	@Test
	public void fromSimpleString() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize("test");
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isString(), is(true));
		assertThat(vpack.getAsString(), is("test"));
	}

	protected static class TestEntityTyped<T> {
		private T e;
	}

	@Test
	public void fromStringTypedEntity() throws VPackException {
		final TestEntityTyped<String> entity = new TestEntityTyped<String>();
		entity.e = "test";
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isString(), is(true));
		assertThat(e.getAsString(), is("test"));
	}

	@Test
	public void fromObjectTypedEntity() throws VPackException {
		final TestEntityTyped<TestEntityString> entity = new TestEntityTyped<VPackSerializeDeserializeTest.TestEntityString>();
		entity.e = new TestEntityString();
		entity.e.s = "test2";
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isObject(), is(true));
		final VPackSlice s = e.get("s");
		assertThat(s, is(notNullValue()));
		assertThat(s.isString(), is(true));
		assertThat(s.getAsString(), is("test2"));
	}

	@Test
	public void fromTypedTypedEntity() throws VPackException {
		final TestEntityTyped<TestEntityTyped<String>> entity = new TestEntityTyped<VPackSerializeDeserializeTest.TestEntityTyped<String>>();
		entity.e = new TestEntityTyped<String>();
		entity.e.e = "test";
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity,
			new SerializeOptions().type(new Type<TestEntityTyped<TestEntityTyped<String>>>() {
			}.getType()));
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice e = vpack.get("e");
		assertThat(e, is(notNullValue()));
		assertThat(e.isObject(), is(true));
		final VPackSlice e2 = e.get("e");
		assertThat(e2, is(notNullValue()));
		assertThat(e2.isString(), is(true));
		assertThat(e2.getAsString(), is("test"));
	}

	@Test
	public void fieldNamingStrategySerialize() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().fieldNamingStrategy(new VPackFieldNamingStrategy() {
			@Override
			public String translateName(final Field field) {
				return "bla";
			}
		}).build().serialize(new TestEntityA());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		final VPackSlice bla = vpack.get("bla");
		assertThat(bla.isString(), is(true));
		assertThat(bla.getAsString(), is("a"));
	}

	@Test
	public void fieldNamingStrategyDeserialize() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("bla", "test");
		builder.close();
		final TestEntityA entity = new VPack.Builder().fieldNamingStrategy(new VPackFieldNamingStrategy() {
			@Override
			public String translateName(final Field field) {
				return "bla";
			}
		}).build().deserialize(builder.slice(), TestEntityA.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.a, is("test"));
	}

	@Test
	public void serializeVPack() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add("test");
		final VPackSlice slice = builder.slice();
		final VPackSlice vpack = new VPack.Builder().build().serialize(slice);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isString(), is(true));
		assertThat(vpack.getAsString(), is("test"));
	}

	@Test
	public void deserializeVPack() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add("test");
		final VPackSlice slice = builder.slice();
		final VPackSlice vpack = new VPack.Builder().build().deserialize(slice, slice.getClass());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isString(), is(true));
		assertThat(vpack.getAsString(), is("test"));
	}

	protected static class TestEntityDate {
		private java.util.Date utilDate = new Date(1474988621);
		private java.sql.Date sqlDate = new java.sql.Date(1474988621);
		private java.sql.Timestamp timestamp = new java.sql.Timestamp(1474988621);

		public java.util.Date getUtilDate() {
			return utilDate;
		}

		public void setUtilDate(final java.util.Date utilDate) {
			this.utilDate = utilDate;
		}

		public java.sql.Date getSqlDate() {
			return sqlDate;
		}

		public void setSqlDate(final java.sql.Date sqlDate) {
			this.sqlDate = sqlDate;
		}

		public java.sql.Timestamp getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(final java.sql.Timestamp timestamp) {
			this.timestamp = timestamp;
		}

	}

	@Test
	public void fromDate() throws VPackException {
		final VPackSlice vpack = new VPack.Builder().build().serialize(new TestEntityDate());
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		{
			assertThat(vpack.get("utilDate").isString(), is(true));
			assertThat(vpack.get("utilDate").getAsString(), is(DATE_FORMAT.format(new Date(1474988621))));
		}
		{
			assertThat(vpack.get("sqlDate").isString(), is(true));
			assertThat(vpack.get("sqlDate").getAsString(), is(DATE_FORMAT.format(new java.sql.Date(1474988621))));
		}
		{
			assertThat(vpack.get("timestamp").isString(), is(true));
			assertThat(vpack.get("timestamp").getAsString(),
				is(DATE_FORMAT.format(new java.sql.Timestamp(1474988621))));
		}
	}

	@Test
	public void toDate() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("utilDate", new Date(1475062216));
		builder.add("sqlDate", new java.sql.Date(1475062216));
		builder.add("timestamp", new java.sql.Timestamp(1475062216));
		builder.close();

		final TestEntityDate entity = new VPack.Builder().build().deserialize(builder.slice(), TestEntityDate.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.utilDate, is(new Date(1475062216)));
		assertThat(entity.sqlDate, is(new java.sql.Date(1475062216)));
		assertThat(entity.timestamp, is(new java.sql.Timestamp(1475062216)));
	}

	@Test
	public void toDateFromString() throws VPackException {
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("utilDate", DATE_FORMAT.format(new Date(1475062216)));
		builder.add("sqlDate", DATE_FORMAT.format(new java.sql.Date(1475062216)));
		builder.add("timestamp", DATE_FORMAT.format(new java.sql.Timestamp(1475062216)));
		builder.close();

		final TestEntityDate entity = new VPack.Builder().build().deserialize(builder.slice(), TestEntityDate.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.utilDate, is(new Date(1475062216)));
		assertThat(entity.sqlDate, is(new java.sql.Date(1475062216)));
		assertThat(entity.timestamp, is(new java.sql.Timestamp(1475062216)));
	}

	protected static class TestEntityUUID {
		private UUID uuid;

		public UUID getUuid() {
			return uuid;
		}

		public void setUuid(final UUID uuid) {
			this.uuid = uuid;
		}
	}

	@Test
	public void fromUUID() {
		final TestEntityUUID entity = new TestEntityUUID();
		entity.setUuid(UUID.randomUUID());
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));

		final VPackSlice uuid = vpack.get("uuid");
		assertThat(uuid.isString(), is(true));
		assertThat(uuid.getAsString(), is(entity.uuid.toString()));
	}

	@Test
	public void toUUID() {
		final UUID uuid = UUID.randomUUID();
		final VPackBuilder builder = new VPackBuilder();
		builder.add(ValueType.OBJECT);
		builder.add("uuid", uuid.toString());
		builder.close();

		final TestEntityUUID entity = new VPack.Builder().build().deserialize(builder.slice(), TestEntityUUID.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.uuid, is(uuid));
	}

	@Test
	public void uuid() {
		final TestEntityUUID entity = new TestEntityUUID();
		entity.setUuid(UUID.randomUUID());
		final VPack vpacker = new VPack.Builder().build();
		final VPackSlice vpack = vpacker.serialize(entity);
		final TestEntityUUID entity2 = vpacker.deserialize(vpack, TestEntityUUID.class);
		assertThat(entity2, is(notNullValue()));
		assertThat(entity2.getUuid(), is(entity.getUuid()));
	}

	private static class TransientEntity {
		private transient String foo;

		public TransientEntity() {
			super();
		}
	}

	@Test
	public void fromTransient() {
		final TransientEntity entity = new TransientEntity();
		entity.foo = "bar";
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get("foo").isNone(), is(true));
	}

	@Test
	public void toTransient() {
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add("foo", "bar").close().slice();
		final TransientEntity entity = new VPack.Builder().build().deserialize(vpack, TransientEntity.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.foo, is(nullValue()));
	}

	private static class BinaryEntity {
		private byte[] foo;

		public BinaryEntity() {
			super();
		}
	}

	@Test
	public void fromBinary() {
		final BinaryEntity entity = new BinaryEntity();
		entity.foo = "bar".getBytes();
		final VPackSlice vpack = new VPack.Builder().build().serialize(entity);
		assertThat(vpack, is(notNullValue()));
		assertThat(vpack.isObject(), is(true));
		assertThat(vpack.get("foo").isString(), is(true));
		assertThat(vpack.get("foo").getAsString(), is(DatatypeConverter.printBase64Binary(entity.foo)));
	}

	@Test
	public void toBinary() {
		final String value = DatatypeConverter.printBase64Binary("bar".getBytes());
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add("foo", value).close().slice();
		final BinaryEntity entity = new VPack.Builder().build().deserialize(vpack, BinaryEntity.class);
		assertThat(entity, is(notNullValue()));
		assertThat(entity.foo, is("bar".getBytes()));
	}

	@Test
	public void asFloatingNumber() {
		final VPackSlice vpack = new VPackBuilder().add(ValueType.OBJECT).add("value", 12000).close().slice();
		assertThat(vpack.get("value").getAsInt(), is(12000));
		assertThat(vpack.get("value").getAsFloat(), is(12000F));
		assertThat(vpack.get("value").getAsDouble(), is(12000.));
	}

	private static class TestEntityObjectKeyMap {
		private Map<TestEntityA, Object> map;

		public TestEntityObjectKeyMap() {
			super();
		}
	}

	@Test
	public void keyMapAdapter() {
		final VPack vpack = new VPack.Builder()
				.registerKeyMapAdapter(TestEntityA.class, new VPackKeyMapAdapter<TestEntityA>() {
					@Override
					public String serialize(final TestEntityA key) {
						return key.getA();
					}

					@Override
					public TestEntityA deserialize(final String key) {
						final TestEntityA e = new TestEntityA();
						e.setA(key);
						return e;
					}
				}).build();

		final TestEntityObjectKeyMap entityIn = new TestEntityObjectKeyMap();
		entityIn.map = new HashMap<TestEntityA, Object>();
		final TestEntityA key = new TestEntityA();
		key.setA("b");
		entityIn.map.put(key, "test");

		final VPackSlice slice = vpack.serialize(entityIn);
		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("map").isObject(), is(true));
		assertThat(slice.get("map").size(), is(1));
		assertThat(slice.get("map").get("b").getAsString(), is("test"));

		final TestEntityObjectKeyMap entityOut = vpack.deserialize(slice, TestEntityObjectKeyMap.class);
		assertThat(entityOut.map.size(), is(1));
		final Entry<TestEntityA, Object> entry = entityOut.map.entrySet().iterator().next();
		assertThat(entry.getKey().a, is("b"));
		assertThat(entry.getValue().toString(), is("test"));
	}

	private static class TestEntityNullHandle1 {
		private TestEntityNullHandle2 e;

		public TestEntityNullHandle1() {
			super();
		}

	}

	private static class TestEntityNullHandle2 {

		public TestEntityNullHandle2() {
			super();
		}

	}

	@Test
	public void customDeserializerWithSelfNullHandle() {
		final VPack vpack = new VPack.Builder()
				.registerDeserializer(TestEntityNullHandle2.class, new VPackDeserializer<TestEntityNullHandle2>() {
					@Override
					public TestEntityNullHandle2 deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						return new TestEntityNullHandle2();
					}
				}, true).serializeNullValues(true).build();
		final TestEntityNullHandle1 entityIn = new TestEntityNullHandle1();
		entityIn.e = null;
		final VPackSlice slice = vpack.serialize(entityIn);
		final TestEntityNullHandle1 entityOut = vpack.deserialize(slice, TestEntityNullHandle1.class);
		assertThat(entityOut.e, is(notNullValue()));
	}

	@Test
	public void customDeserializerWithoutSelfNullHandle() {
		final VPack vpack = new VPack.Builder()
				.registerDeserializer(TestEntityNullHandle2.class, new VPackDeserializer<TestEntityNullHandle2>() {
					@Override
					public TestEntityNullHandle2 deserialize(
						final VPackSlice parent,
						final VPackSlice vpack,
						final VPackDeserializationContext context) throws VPackException {
						return new TestEntityNullHandle2();
					}
				}, false).serializeNullValues(true).build();
		final TestEntityNullHandle1 entityIn = new TestEntityNullHandle1();
		entityIn.e = null;
		final VPackSlice slice = vpack.serialize(entityIn);
		final TestEntityNullHandle1 entityOut = vpack.deserialize(slice, TestEntityNullHandle1.class);
		assertThat(entityOut.e, is(nullValue()));
	}

	static class TestEntityTypeInfo {

	}

	static class TestEntityGenericA<T> {
		T value;
	}

	@Test
	public void genericType() {
		final TestEntityGenericA<TestEntityTypeInfo> entity = new TestEntityGenericA<TestEntityTypeInfo>();
		entity.value = new TestEntityTypeInfo();

		final VPack vpack = new VPack.Builder().build();
		final VPackSlice slice = vpack.serialize(entity);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isObject(), is(true));
		assertThat(slice.get("value").get("_class").isString(), is(true));
		assertThat(slice.get("value").get("_class").getAsString(),
			is("com.arangodb.velocypack.VPackSerializeDeserializeTest$TestEntityTypeInfo"));

		final TestEntityGenericA<TestEntityTypeInfo> entityOut = vpack.deserialize(slice, TestEntityGenericA.class);
		assertThat(entityOut, is(notNullValue()));
		assertThat(entityOut.value, is(notNullValue()));
		assertThat(entityOut.value instanceof TestEntityTypeInfo, is(true));
	}

	static class TestEntityGenericList<T> {
		List<T> value;
	}

	@Test
	public void genericList() {
		final TestEntityGenericList<TestEntityTypeInfo> entity = new TestEntityGenericList<TestEntityTypeInfo>();
		entity.value = new ArrayList<TestEntityTypeInfo>();
		entity.value.add(new TestEntityTypeInfo());

		final VPack vpack = new VPack.Builder().build();
		final VPackSlice slice = vpack.serialize(entity);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("value").get(0).isObject(), is(true));
		assertThat(slice.get("value").get(0).get("_class").isString(), is(true));
		assertThat(slice.get("value").get(0).get("_class").getAsString(),
			is("com.arangodb.velocypack.VPackSerializeDeserializeTest$TestEntityTypeInfo"));

		final TestEntityGenericList<TestEntityTypeInfo> entityOut = vpack.deserialize(slice,
			TestEntityGenericList.class);
		assertThat(entityOut, is(notNullValue()));
		assertThat(entityOut.value, is(notNullValue()));
		assertThat(entityOut.value.size(), is(1));
		assertThat(entityOut.value.get(0) instanceof TestEntityTypeInfo, is(true));
	}

	static class TestEntityObjectList {
		List<Object> value;
	}

	@Test
	public void objectList() {
		final TestEntityObjectList entity = new TestEntityObjectList();
		entity.value = new ArrayList<Object>();
		entity.value.add(new TestEntityTypeInfo());

		final VPack vpack = new VPack.Builder().build();
		final VPackSlice slice = vpack.serialize(entity);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("value").get(0).isObject(), is(true));
		assertThat(slice.get("value").get(0).get("_class").isString(), is(true));
		assertThat(slice.get("value").get(0).get("_class").getAsString(),
			is("com.arangodb.velocypack.VPackSerializeDeserializeTest$TestEntityTypeInfo"));

		final TestEntityObjectList entityOut = vpack.deserialize(slice, TestEntityObjectList.class);
		assertThat(entityOut, is(notNullValue()));
		assertThat(entityOut.value, is(notNullValue()));
		assertThat(entityOut.value.size(), is(1));
		assertThat(entityOut.value.get(0) instanceof TestEntityTypeInfo, is(true));
	}

	static class TestEntityObjectArray {
		Object[] value;
	}

	@Test
	public void heterogeneousArray() {
		final TestEntityObjectArray entity = new TestEntityObjectArray();
		entity.value = new Object[]{new TestEntityTypeInfo()};

		final VPack vpack = new VPack.Builder().build();
		final VPackSlice slice = vpack.serialize(entity);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("value").get(0).isObject(), is(true));
		assertThat(slice.get("value").get(0).get("_class").isString(), is(true));
		assertThat(slice.get("value").get(0).get("_class").getAsString(),
			is("com.arangodb.velocypack.VPackSerializeDeserializeTest$TestEntityTypeInfo"));

		final TestEntityObjectArray entityOut = vpack.deserialize(slice, TestEntityObjectArray.class);
		assertThat(entityOut, is(notNullValue()));
		assertThat(entityOut.value, is(notNullValue()));
		assertThat(entityOut.value.length, is(1));
		assertThat(entityOut.value[0] instanceof TestEntityTypeInfo, is(true));
	}

	static class TestEntityTypeInfoArray {
		TestEntityTypeInfo[] value;
	}

	@Test
	public void homogeneousArray() {
		final TestEntityTypeInfoArray entity = new TestEntityTypeInfoArray();
		entity.value = new TestEntityTypeInfo[]{new TestEntityTypeInfo()};

		final VPack vpack = new VPack.Builder().build();
		final VPackSlice slice = vpack.serialize(entity);

		assertThat(slice.isObject(), is(true));
		assertThat(slice.get("value").isArray(), is(true));
		assertThat(slice.get("value").get(0).isObject(), is(true));
		assertThat(slice.get("value").get(0).get("_class").isNone(), is(true));

		final TestEntityTypeInfoArray entityOut = vpack.deserialize(slice, TestEntityTypeInfoArray.class);
		assertThat(entityOut, is(notNullValue()));
		assertThat(entityOut.value, is(notNullValue()));
		assertThat(entityOut.value.length, is(1));
		assertThat(entityOut.value[0], notNullValue());
	}

	public static class Limb {
	}

	public static class Leg extends Limb {

		public String name;

		public Leg() {
		}

		public Leg(final String name) {
			this.name = name;
		}
	}

	public static abstract class Animal<L extends Limb> {
		public List<L> arms;
		public L rightLeg;
	}

	public static class Husky extends Animal<Leg> {
	}

	@Test
	public void huskyTestWauWau() {
		final VPack vpack = new VPack.Builder().build();
		final Husky tom = new Husky();
		tom.rightLeg = new Leg("right leg");
		tom.arms = new ArrayList<Leg>(Arrays.asList(new Leg("right arm"), new Leg("left arm")));

		final Husky tomInMirror = vpack.deserialize(vpack.serialize(tom), Husky.class);
		assertThat(tomInMirror, is(notNullValue()));
		assertThat(tomInMirror.rightLeg instanceof Leg, is(true));
		assertThat(tomInMirror.rightLeg.name, is("right leg"));
		assertThat(tomInMirror.arms.size(), is(2));
	}

}
