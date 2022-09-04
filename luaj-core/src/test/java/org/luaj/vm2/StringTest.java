package org.luaj.vm2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;

class StringTest {

	@Test
	void testToInputStream() throws IOException {
		LuaString str = LuaString.valueOf("Hello");

		InputStream is = str.toInputStream();

		assertEquals('H', is.read());
		assertEquals('e', is.read());
		assertEquals(2, is.skip(2));
		assertEquals('o', is.read());
		assertEquals(-1, is.read());

		assertTrue(is.markSupported());

		is.reset();

		assertEquals('H', is.read());
		is.mark(4);

		assertEquals('e', is.read());
		is.reset();
		assertEquals('e', is.read());

		LuaString substr = str.substring(1, 4);
		assertEquals(3, substr.length());

		is.close();
		is = substr.toInputStream();

		assertEquals('e', is.read());
		assertEquals('l', is.read());
		assertEquals('l', is.read());
		assertEquals(-1, is.read());

		is = substr.toInputStream();
		is.reset();

		assertEquals('e', is.read());
	}

	private static final String userFriendly(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, n = s.length(); i < n; i++) {
			int c = s.charAt(i);
			if (c < ' ' || c >= 0x80) {
				sb.append("\\u" + Integer.toHexString(0x10000+c).substring(1));
			} else {
				sb.append((char) c);
			}
		}
		return sb.toString();
	}

	@Test
	void testUtf820482051() throws UnsupportedEncodingException {
		int i = 2048;
		char[] c = { (char) (i+0), (char) (i+1), (char) (i+2), (char) (i+3) };
		String before = new String(c) + " " + i + "-" + (i+4);
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals(userFriendly(before), userFriendly(after));
	}

	@Test
	void testUtf8() {
		for (int i = 4; i < 0xffff; i += 4) {
			char[] c = { (char) (i+0), (char) (i+1), (char) (i+2), (char) (i+3) };
			String before = new String(c) + " " + i + "-" + (i+4);
			LuaString ls = LuaString.valueOf(before);
			String after = ls.tojstring();
			assertEquals(userFriendly(before), userFriendly(after));
		}
		char[] c = { (char) (1), (char) (2), (char) (3) };
		String before = new String(c) + " 1-3";
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals(userFriendly(before), userFriendly(after));
	}

	@Test
	void testSpotCheckUtf8() throws UnsupportedEncodingException {
		byte[] bytes = { (byte) 194, (byte) 160, (byte) 194, (byte) 161, (byte) 194, (byte) 162, (byte) 194, (byte) 163,
				(byte) 194, (byte) 164 };
		String expected = new String(bytes, "UTF8");
		String actual = LuaString.valueOf(bytes).tojstring();
		char[] d = actual.toCharArray();
		assertEquals(160, d[0]);
		assertEquals(161, d[1]);
		assertEquals(162, d[2]);
		assertEquals(163, d[3]);
		assertEquals(164, d[4]);
		assertEquals(expected, actual);
	}

	@Test
	void testNullTerminated() {
		char[] c = { 'a', 'b', 'c', '\0', 'd', 'e', 'f' };
		String before = new String(c);
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals(userFriendly("abc\0def"), userFriendly(after));
	}

	@Test
	void testRecentStringsCacheDifferentHashcodes() {
		final byte[] abc = { 'a', 'b', 'c' };
		final byte[] xyz = { 'x', 'y', 'z' };
		final LuaString abc1 = LuaString.valueOf(abc);
		final LuaString xyz1 = LuaString.valueOf(xyz);
		final LuaString abc2 = LuaString.valueOf(abc);
		final LuaString xyz2 = LuaString.valueOf(xyz);
		final int mod = LuaString.RECENT_STRINGS_CACHE_SIZE;
		assertTrue(abc1.hashCode()%mod != xyz1.hashCode()%mod);
		assertSame(abc1, abc2);
		assertSame(xyz1, xyz2);
	}

	@Test
	void testRecentStringsCacheHashCollisionCacheHit() {
		final byte[] abc = { 'a', 'b', 'c' };
		final byte[] lyz = { 'l', 'y', 'z' }; // chosen to have hash collision with 'abc'
		final LuaString abc1 = LuaString.valueOf(abc);
		final LuaString abc2 = LuaString.valueOf(abc); // in cache: 'abc'
		final LuaString lyz1 = LuaString.valueOf(lyz);
		final LuaString lyz2 = LuaString.valueOf(lyz); // in cache: 'lyz'
		final int mod = LuaString.RECENT_STRINGS_CACHE_SIZE;
		assertEquals(abc1.hashCode()%mod, lyz1.hashCode()%mod);
		assertNotSame(abc1, lyz1);
		assertFalse(abc1.equals(lyz1));
		assertSame(abc1, abc2);
		assertSame(lyz1, lyz2);
	}

	@Test
	void testRecentStringsCacheHashCollisionCacheMiss() {
		final byte[] abc = { 'a', 'b', 'c' };
		final byte[] lyz = { 'l', 'y', 'z' }; // chosen to have hash collision with 'abc'
		final LuaString abc1 = LuaString.valueOf(abc);
		final LuaString lyz1 = LuaString.valueOf(lyz); // in cache: 'abc'
		final LuaString abc2 = LuaString.valueOf(abc); // in cache: 'lyz'
		final LuaString lyz2 = LuaString.valueOf(lyz); // in cache: 'abc'
		final int mod = LuaString.RECENT_STRINGS_CACHE_SIZE;
		assertEquals(abc1.hashCode()%mod, lyz1.hashCode()%mod);
		assertNotSame(abc1, lyz1);
		assertFalse(abc1.equals(lyz1));
		assertNotSame(abc1, abc2);
		assertNotSame(lyz1, lyz2);
	}

	@Test
	void testRecentStringsLongStrings() {
		byte[] abc = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes();
		assertTrue(abc.length > LuaString.RECENT_STRINGS_MAX_LENGTH);
		LuaString abc1 = LuaString.valueOf(abc);
		LuaString abc2 = LuaString.valueOf(abc);
		assertNotSame(abc1, abc2);
	}

	@Test
	void testRecentStringsUsingJavaStrings() {
		final String abc = "abc";
		final String lyz = "lyz"; // chosen to have hash collision with 'abc'
		final String xyz = "xyz";

		final LuaString abc1 = LuaString.valueOf(abc);
		final LuaString abc2 = LuaString.valueOf(abc);
		final LuaString lyz1 = LuaString.valueOf(lyz);
		final LuaString lyz2 = LuaString.valueOf(lyz);
		final LuaString xyz1 = LuaString.valueOf(xyz);
		final LuaString xyz2 = LuaString.valueOf(xyz);
		final int mod = LuaString.RECENT_STRINGS_CACHE_SIZE;
		assertEquals(abc1.hashCode()%mod, lyz1.hashCode()%mod);
		assertFalse(abc1.hashCode()%mod == xyz1.hashCode()%mod);
		assertSame(abc1, abc2);
		assertSame(lyz1, lyz2);
		assertSame(xyz1, xyz2);

		final LuaString abc3 = LuaString.valueOf(abc);
		final LuaString lyz3 = LuaString.valueOf(lyz);
		final LuaString xyz3 = LuaString.valueOf(xyz);

		final LuaString abc4 = LuaString.valueOf(abc);
		final LuaString lyz4 = LuaString.valueOf(lyz);
		final LuaString xyz4 = LuaString.valueOf(xyz);
		assertNotSame(abc3, abc4); // because of hash collision
		assertNotSame(lyz3, lyz4); // because of hash collision
		assertSame(xyz3, xyz4); // because hashes do not collide
	}

	@Test
	void testLongSubstringGetsOldBacking() {
		LuaString src = LuaString.valueOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		LuaString sub1 = src.substring(10, 40);
		assertSame(src.m_bytes, sub1.m_bytes);
		assertEquals(sub1.m_offset, 10);
		assertEquals(sub1.m_length, 30);
	}

	@Test
	void testShortSubstringGetsNewBacking() {
		LuaString src = LuaString.valueOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		LuaString sub1 = src.substring(10, 20);
		LuaString sub2 = src.substring(10, 20);
		assertEquals(sub1.m_offset, 0);
		assertEquals(sub1.m_length, 10);
		assertSame(sub1, sub2);
		assertFalse(src.m_bytes == sub1.m_bytes);
	}

	@Test
	void testShortSubstringOfVeryLongStringGetsNewBacking() {
		LuaString src = LuaString.valueOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
			+ "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
		LuaString sub1 = src.substring(10, 50);
		LuaString sub2 = src.substring(10, 50);
		assertEquals(sub1.m_offset, 0);
		assertEquals(sub1.m_length, 40);
		assertFalse(sub1 == sub2);
		assertFalse(src.m_bytes == sub1.m_bytes);
	}

	@Test
	void testIndexOfByteInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		assertEquals(6, str.indexOf((byte) ':', 0));
		assertEquals(6, str.indexOf((byte) ':', 2));
		assertEquals(6, str.indexOf((byte) ':', 6));
		assertEquals(-1, str.indexOf((byte) ':', 7));
		assertEquals(-1, str.indexOf((byte) ':', 9));
		assertEquals(9, str.indexOf((byte) 'i', 0));
		assertEquals(9, str.indexOf((byte) 'i', 2));
		assertEquals(9, str.indexOf((byte) 'i', 9));
		assertEquals(-1, str.indexOf((byte) 'z', 0));
		assertEquals(-1, str.indexOf((byte) 'z', 2));
		assertEquals(-1, str.indexOf((byte) 'z', 9));

		assertEquals(4, sub.indexOf((byte) ':', 0));
		assertEquals(4, sub.indexOf((byte) ':', 2));
		assertEquals(4, sub.indexOf((byte) ':', 4));
		assertEquals(-1, sub.indexOf((byte) ':', 5));
		assertEquals(-1, sub.indexOf((byte) ':', 7));
		assertEquals(7, sub.indexOf((byte) 'i', 0));
		assertEquals(7, sub.indexOf((byte) 'i', 2));
		assertEquals(7, sub.indexOf((byte) 'i', 7));
		assertEquals(-1, sub.indexOf((byte) 'z', 0));
		assertEquals(-1, sub.indexOf((byte) 'z', 2));
		assertEquals(-1, sub.indexOf((byte) 'z', 7));
	}

	@Test
	void testIndexOfPatternInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString pat = LuaString.valueOf(":");
		LuaString i = LuaString.valueOf("i");
		LuaString xyz = LuaString.valueOf("xyz");

		assertEquals(6, str.indexOf(pat, 0));
		assertEquals(6, str.indexOf(pat, 2));
		assertEquals(6, str.indexOf(pat, 6));
		assertEquals(-1, str.indexOf(pat, 7));
		assertEquals(-1, str.indexOf(pat, 9));
		assertEquals(9, str.indexOf(i, 0));
		assertEquals(9, str.indexOf(i, 2));
		assertEquals(9, str.indexOf(i, 9));
		assertEquals(-1, str.indexOf(xyz, 0));
		assertEquals(-1, str.indexOf(xyz, 2));
		assertEquals(-1, str.indexOf(xyz, 9));

		assertEquals(4, sub.indexOf(pat, 0));
		assertEquals(4, sub.indexOf(pat, 2));
		assertEquals(4, sub.indexOf(pat, 4));
		assertEquals(-1, sub.indexOf(pat, 5));
		assertEquals(-1, sub.indexOf(pat, 7));
		assertEquals(7, sub.indexOf(i, 0));
		assertEquals(7, sub.indexOf(i, 2));
		assertEquals(7, sub.indexOf(i, 7));
		assertEquals(-1, sub.indexOf(xyz, 0));
		assertEquals(-1, sub.indexOf(xyz, 2));
		assertEquals(-1, sub.indexOf(xyz, 7));
	}

	@Test
	void testLastIndexOfPatternInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString pat = LuaString.valueOf(":");
		LuaString i = LuaString.valueOf("i");
		LuaString xyz = LuaString.valueOf("xyz");

		assertEquals(6, str.lastIndexOf(pat));
		assertEquals(9, str.lastIndexOf(i));
		assertEquals(-1, str.lastIndexOf(xyz));

		assertEquals(4, sub.lastIndexOf(pat));
		assertEquals(7, sub.lastIndexOf(i));
		assertEquals(-1, sub.lastIndexOf(xyz));
	}

	@Test
	void testIndexOfAnyInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString ghi = LuaString.valueOf("ghi");
		LuaString ihg = LuaString.valueOf("ihg");
		LuaString ijk = LuaString.valueOf("ijk");
		LuaString kji = LuaString.valueOf("kji");
		LuaString xyz = LuaString.valueOf("xyz");
		LuaString ABCdEFGHIJKL = LuaString.valueOf("ABCdEFGHIJKL");
		LuaString EFGHIJKL = ABCdEFGHIJKL.substring(4, 12);
		LuaString CdEFGHIJ = ABCdEFGHIJKL.substring(2, 10);
		assertEquals(4, EFGHIJKL.m_offset);
		assertEquals(2, CdEFGHIJ.m_offset);

		assertEquals(7, str.indexOfAny(ghi));
		assertEquals(7, str.indexOfAny(ihg));
		assertEquals(9, str.indexOfAny(ijk));
		assertEquals(9, str.indexOfAny(kji));
		assertEquals(-1, str.indexOfAny(xyz));
		assertEquals(3, str.indexOfAny(CdEFGHIJ));
		assertEquals(-1, str.indexOfAny(EFGHIJKL));

		assertEquals(5, sub.indexOfAny(ghi));
		assertEquals(5, sub.indexOfAny(ihg));
		assertEquals(7, sub.indexOfAny(ijk));
		assertEquals(7, sub.indexOfAny(kji));
		assertEquals(-1, sub.indexOfAny(xyz));
		assertEquals(1, sub.indexOfAny(CdEFGHIJ));
		assertEquals(-1, sub.indexOfAny(EFGHIJKL));
	}
}
