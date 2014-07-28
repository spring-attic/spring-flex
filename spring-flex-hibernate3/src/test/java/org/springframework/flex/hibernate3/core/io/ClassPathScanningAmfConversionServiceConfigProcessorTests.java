package org.springframework.flex.hibernate3.core.io;


import java.util.regex.Pattern;

import javax.persistence.Embeddable;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.flex.core.io.ClassPathScanningAmfConversionServiceConfigProcessor;
import org.springframework.flex.core.io.SpringPropertyProxy;
import org.springframework.flex.hibernate3.core.AbstractMessageBrokerTests;
import org.springframework.flex.hibernate3.core.io.domain.EmbeddedAddress;
import org.springframework.flex.hibernate3.core.io.domain.Person;
import org.springframework.flex.hibernate3.core.io.domain.PersonNP;

import flex.messaging.io.PropertyProxyRegistry;

public class ClassPathScanningAmfConversionServiceConfigProcessorTests extends AbstractMessageBrokerTests {

	@Test
	public void basicPackageScan() throws Exception {
		setDirty();
		ClassPathScanningAmfConversionServiceConfigProcessor configProcessor = new ClassPathScanningAmfConversionServiceConfigProcessor("org.springframework.flex.hibernate3.core.io.domain");
		configProcessor.setBeanClassLoader(getApplicationContext().getClassLoader());
		configProcessor.setResourceLoader(getApplicationContext());
		configProcessor.afterPropertiesSet();

		addStartupProcessor(configProcessor);
		getMessageBroker();

		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new Person()));
		Assert.assertTrue(PropertyProxyRegistry.getProxy(new Person()) instanceof SpringPropertyProxy);
	}

	@Test
	public void packageScanWithRegexIncludeFilter() throws Exception {
		setDirty();
		ClassPathScanningAmfConversionServiceConfigProcessor configProcessor = new ClassPathScanningAmfConversionServiceConfigProcessor("org.springframework.flex.hibernate3.core.io.domain");
		configProcessor.setBeanClassLoader(getApplicationContext().getClassLoader());
		configProcessor.setResourceLoader(getApplicationContext());
		configProcessor.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*NP")));
		configProcessor.afterPropertiesSet();

		addStartupProcessor(configProcessor);
		getMessageBroker();

		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new Person()));
		Assert.assertFalse(PropertyProxyRegistry.getProxy(new Person()) instanceof SpringPropertyProxy);
		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new PersonNP()));
		Assert.assertTrue(PropertyProxyRegistry.getProxy(new PersonNP()) instanceof SpringPropertyProxy);
	}

	@Test
	public void packageScanWithRegexExcludeFilter() throws Exception {
		setDirty();
		ClassPathScanningAmfConversionServiceConfigProcessor configProcessor = new ClassPathScanningAmfConversionServiceConfigProcessor("org.springframework.flex.hibernate3.core.io.domain");
		configProcessor.setBeanClassLoader(getApplicationContext().getClassLoader());
		configProcessor.setResourceLoader(getApplicationContext());
		configProcessor.addExcludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*NP")));
		configProcessor.afterPropertiesSet();

		addStartupProcessor(configProcessor);
		getMessageBroker();

		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new Person()));
		Assert.assertTrue(PropertyProxyRegistry.getProxy(new Person()) instanceof SpringPropertyProxy);
		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new PersonNP()));
		Assert.assertFalse(PropertyProxyRegistry.getProxy(new PersonNP()) instanceof SpringPropertyProxy);
	}

	@Test
	public void packageScanWithAnnotationIncludeFilter() throws Exception {
		setDirty();
		ClassPathScanningAmfConversionServiceConfigProcessor configProcessor = new ClassPathScanningAmfConversionServiceConfigProcessor("org.springframework.flex.hibernate3.core.io.domain");
		configProcessor.setBeanClassLoader(getApplicationContext().getClassLoader());
		configProcessor.setResourceLoader(getApplicationContext());
		configProcessor.addIncludeFilter(new AnnotationTypeFilter(Embeddable.class));
		configProcessor.afterPropertiesSet();

		addStartupProcessor(configProcessor);
		getMessageBroker();

		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new Person()));
		Assert.assertFalse(PropertyProxyRegistry.getProxy(new Person()) instanceof SpringPropertyProxy);
		Assert.assertNotNull(PropertyProxyRegistry.getProxy(new EmbeddedAddress()));
		Assert.assertTrue(PropertyProxyRegistry.getProxy(new EmbeddedAddress()) instanceof SpringPropertyProxy);
	}
}
