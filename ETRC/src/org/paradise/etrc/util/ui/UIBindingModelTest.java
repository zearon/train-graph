package org.paradise.etrc.util.ui;
import static org.junit.Assert.*;

import static org.paradise.etrc.ETRC.__;

import static org.paradise.etrc.ETRCUtil.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paradise.etrc.data.GlobalSettings;
import org.paradise.etrc.data.TrainGraphFactory;

public class UIBindingModelTest {
	GlobalSettings settings;

	public UIBindingModelTest() {
	}

	@Before
	public void setUp() throws Exception {
		settings = TrainGraphFactory.createInstance(GlobalSettings.class);
//		UIBindingFactory.setCacheEnabledForSameProperty(false);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testIntFieldGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "displayLevel", "", null, true);
		
		int expected = 100;
		settings.displayLevel = expected;
		
		int actual = (int) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIntFieldSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "displayLevel", "", null, true);
		
		int expected = 100;
		model.setModelValue(expected);
		int actual = settings.displayLevel;
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFloatFieldGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "minuteScale", "", null, true);
		
		float expected = 7.5f;
		settings.minuteScale = expected;
		
		float actual = (float) model.getModelValue();
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testFloatFieldSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "minuteScale", "", null, true);
		
		float expected = 7.5f;
		model.setModelValue(expected);
		float actual = settings.minuteScale;
		
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testStringFieldGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "name", "", null, true);
		
		String expected = "test string";
		settings.name = expected;
		
		Object actual = (String) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testStringFieldSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "name", "", null, true);
		
		String expected = "test string";
		model.setModelValue(expected);
		Object actual = settings.name;
		
		assertEquals(expected, actual);
	}
	
	
	
	

	
	@Test
	public void testIntMethodGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "displayLevel", "", null, false);
		
		int expected = 100;
		settings.displayLevel = expected;
		
		int actual = (int) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIntMethodSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "displayLevel", "", null, false);
		
		int expected = 100;
		model.setModelValue(expected);
		int actual = settings.displayLevel;
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFloatMethodGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "minuteScale", "", null, false);
		
		float expected = 7.5f;
		settings.minuteScale = expected;
		
		float actual = (float) model.getModelValue();
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testFloatMethodSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "minuteScale", "", null, false);
		
		float expected = 7.5f;
		model.setModelValue(expected);
		float actual = settings.minuteScale;
		
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testStringMethodGetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "name", "", null, false);
		
		String expected = "test string";
		settings.name = expected;
		
		Object actual = (String) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testStringMethodSetter() {
		UIBinding model = UIBindingFactory.getUIBinding(settings, "name", "", null, false);
		
		String expected = "test string";
		model.setModelValue(expected);
		Object actual = settings.name;
		
		assertEquals(expected, actual);
	}

}
