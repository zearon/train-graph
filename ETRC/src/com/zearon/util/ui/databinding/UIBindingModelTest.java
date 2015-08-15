package com.zearon.util.ui.databinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.paradise.etrc.data.TrainGraphFactory;
import org.paradise.etrc.data.v1.ChartSettings;

import static org.junit.Assert.assertEquals;

public class UIBindingModelTest {
	ChartSettings settings;

	public UIBindingModelTest() {
	}

	@Before
	public void setUp() throws Exception {
		settings = TrainGraphFactory.createInstance(ChartSettings.class);
//		UIBindingFactory.setCacheEnabledForSameProperty(false);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testIntFieldGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "displayLevel", "", null, true);
		
		int expected = 100;
		settings.displayLevel = expected;
		
		int actual = (int) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIntFieldSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "displayLevel", "", null, true);
		
		int expected = 100;
		model.setModelValue(expected);
		int actual = settings.displayLevel;
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFloatFieldGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "minuteScale", "", null, true);
		
		float expected = 7.5f;
		settings.minuteScale = expected;
		
		float actual = (float) model.getModelValue();
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testFloatFieldSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "minuteScale", "", null, true);
		
		float expected = 7.5f;
		model.setModelValue(expected);
		float actual = settings.minuteScale;
		
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testStringFieldGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "name", "", null, true);
		
		String expected = "test string";
		settings.setName(expected);
		
		Object actual = (String) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testStringFieldSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "name", "", null, true);
		
		String expected = "test string";
		model.setModelValue(expected);
		Object actual = settings.getName();
		
		assertEquals(expected, actual);
	}
	
	
	
	

	
	@Test
	public void testIntMethodGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "displayLevel", "", null, false);
		
		int expected = 100;
		settings.displayLevel = expected;
		
		int actual = (int) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testIntMethodSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "displayLevel", "", null, false);
		
		int expected = 100;
		model.setModelValue(expected);
		int actual = settings.displayLevel;
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void testFloatMethodGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "minuteScale", "", null, false);
		
		float expected = 7.5f;
		settings.minuteScale = expected;
		
		float actual = (float) model.getModelValue();
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testFloatMethodSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "minuteScale", "", null, false);
		
		float expected = 7.5f;
		model.setModelValue(expected);
		float actual = settings.minuteScale;
		
		assertEquals(expected, actual, 0);
	}
	
	@Test
	public void testStringMethodGetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "name", "", null, false);
		
		String expected = "test string";
		settings.setName(expected);
		
		Object actual = (String) model.getModelValue();
		assertEquals(expected, actual);
	}
	
	@Test
	public void testStringMethodSetter() {
		UIBinding model = UIBindingManager.getUIBinding(settings, "name", "", null, false);
		
		String expected = "test string";
		model.setModelValue(expected);
		Object actual = settings.getName();
		
		assertEquals(expected, actual);
	}

}
