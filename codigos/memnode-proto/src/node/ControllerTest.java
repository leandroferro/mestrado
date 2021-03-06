package node;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;


public class ControllerTest {

	private Controller controller;
	
	@Before
	public void setUp() {
		this.controller = new Controller();
	}
	
	@Test
	public void checkWriteAndRead() {
		
		byte original[] = {1, 2, 3, 4, 5};
		
		{
			byte data[] = Arrays.copyOfRange(original, 0, original.length);
			controller.write(data, 15);
			byte actual[] = controller.read(15, data.length);
		
			Assert.assertTrue(Arrays.toString(data) + " != " + Arrays.toString(actual), Arrays.equals(data, actual));
		}
		
		{
			byte data[] = Arrays.copyOfRange(original, 0, 2);
			controller.write(data, 1022);
			byte actual[] = controller.read(1022, data.length);
		
			Assert.assertTrue(Arrays.toString(data) + " != " + Arrays.toString(actual), Arrays.equals(data, actual));
		}
		
		{
			byte data[] = Arrays.copyOfRange(original, 0, 2);
			byte expected[] = Arrays.copyOfRange(original, 0, 1);
			controller.write(data, 1023);
			byte actual[] = controller.read(1023, 1);
		
			Assert.assertTrue(Arrays.toString(expected) + " != " + Arrays.toString(actual), Arrays.equals(expected, actual));
		}
		
		{
			byte data[] = Arrays.copyOfRange(original, 0, original.length);
			byte expected[] = Arrays.copyOfRange(original, 0, 1);
			controller.write(data, 1023);
			byte actual[] = controller.read(1023, data.length);
		
			Assert.assertTrue(Arrays.toString(expected) + " != " + Arrays.toString(actual), Arrays.equals(expected, actual));
		}
	}
	
	@Test(expected=InvalidPosition.class)
	public void testErrorOnWrite() {
		byte original[] = {1, 2, 3, 4, 5};
		
		controller.write(original, 2048);
	}
	
	@Test(expected=InvalidPosition.class)
	public void testErrorOnWriteWithNegativePosition() {
		byte original[] = {1, 2, 3, 4, 5};
		
		controller.write(original, -1);
	}
	
	@Test(expected=InvalidPosition.class)
	public void testErrorOnRead() {
		controller.read(2048, 1);
	}
}
