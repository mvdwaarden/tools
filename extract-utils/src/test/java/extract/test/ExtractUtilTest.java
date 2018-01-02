package extract.test;

import org.junit.Assert;
import org.junit.Test;

import extract.LazySource;

public class ExtractUtilTest {
	@Test
	public void simpleTest() {
		Assert.assertTrue("dummy test", true);
	}

	@Test
	public void testLazy() {
		StringSource src = new StringSource("1234567686966");
		StringBuffer assertSequenceTest = new StringBuffer();
		StringBuffer assertResultTest = new StringBuffer();
		LazySource chain = src.filter("246", c -> {
			assertSequenceTest.append("1" + c);
			return c == '6' || c == '4' || c == '2';
		}).filter("6", c -> {
			assertSequenceTest.append("2" + c);
			return c == '6';
		}).filter("sequence test", c -> {
			assertSequenceTest.append("3" + c);
			assertResultTest.append("" + c);
			return true;
		});		
		chain.go();
 		Assert.assertEquals("call sequence",assertSequenceTest.toString(),"11122213142415162636171626361816263619162636162636");
		Assert.assertEquals("result test",assertResultTest.toString(),"66666");
		src = new StringSource("1234567686966");
		assertSequenceTest.delete(0, assertSequenceTest.length());
		assertResultTest.delete(0,assertResultTest.length());
		Character cTmp = src.filter("246", c -> {
			assertSequenceTest.append("1" + c);
			return c == '6' || c == '4' || c == '2';
		}).filter("6", c -> {
			assertSequenceTest.append("2" + c);		
			return c == '6';
		}).filter("sequence test", c -> {			
			assertSequenceTest.append("3" + c);		
			assertResultTest.append("" + c);
			return true;
		}).findFirst();		
		Assert.assertEquals("call sequence",assertSequenceTest.toString(),"11122213142415162636");
		Assert.assertEquals("result test",assertResultTest.toString(),"6");
		src = new StringSource("1234567686966");
		assertSequenceTest.delete(0, assertSequenceTest.length());
		assertResultTest.delete(0,assertResultTest.length());
		int tmpInt = src.filter("non 5's", tc -> {
			assertSequenceTest.append("1" + tc);
			boolean result = tc.charValue() == '5';
			return result;
		}).map("2int", tc -> {
			assertSequenceTest.append("2" + tc);
			Integer result = (Integer) 1000 * (tc.charValue() - '0');
			return result;
		}).findFirst();
		Assert.assertEquals("call sequence",assertSequenceTest.toString(),"111213141525");
		Assert.assertEquals("result test",tmpInt, 5000);	

	}

	public <T> void print(T t) {
		System.out.print(t);
	}

	public class StringSource extends LazySource<Character> {
		private String str;
		private int idx;

		public StringSource(String str) {
			super("string", null);
			this.str = str;
			this.idx = 0;
		}

		@Override
		public Character nextElement() {
			if (idx < str.length())
				return str.charAt(idx++);
			else
				return null;
		}
	}
}
