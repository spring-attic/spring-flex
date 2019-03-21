/*
 * Copyright 2002-20011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.core.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * Generic extension of {@link AbstractAmfConversionServiceConfigProcessor} that configures the Spring {@link ConversionService}-based AMF 
 * serialization/deserialization support via classpath scanning.
 * 
 * <p>
 * This implementation will recursively scan for classes starting with the base package provided in the 
 * {@link ClassPathScanningAmfConversionServiceConfigProcessor#ClassPathScanningAmfConversionServiceConfigProcessor(String) constructor}.  
 * By default, a {@link SpringPropertyProxy} will be registered for every class found in the scan.
 * 
 * <p>
 * The scanning process may be more finely tuned by providing {@link TypeFilter} implementations to be used in 
 * {@link ClassPathScanningAmfConversionServiceConfigProcessor#setIncludeFilters(List) including} and 
 * {@link ClassPathScanningAmfConversionServiceConfigProcessor#setExcludeFilters(List) excluding} specific classes.  For example, you 
 * can filter by {@link RegexPatternTypeFilter RegEx patterns}, {@link AnnotationTypeFilter custom annotations}, or anything else that 
 * can be used for matching in a {@code TypeFilter}.
 * 
 * <p>
 * This implementation does not register any additional {@link Converter Converters} beyond those registered in the parent class.  
 * Additional {@code TypeConverters} may be registered by extending this class and overriding 
 * {@link AbstractAmfConversionServiceConfigProcessor#configureConverters(org.springframework.core.convert.converter.ConverterRegistry) configureConverters}.
 * 
 * <p>
 * The implementation is heavily derived from {@link ClassPathScanningCandidateComponentProvider}.
 * 
 * @author Jeremy Grelle
 */
public class ClassPathScanningAmfConversionServiceConfigProcessor extends AbstractAmfConversionServiceConfigProcessor implements ResourceLoaderAware, BeanClassLoaderAware {

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
    
    private ClassLoader beanClassLoader;

    private List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

    private List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
    
    private final String basePackage;
    
    /**
     * Create a ClassPathScanningAmfConversionServiceConfigProcessor
     * @param basePackage the base package to scan for classes to be registered for AMF serialization/deserialization
     */
    public ClassPathScanningAmfConversionServiceConfigProcessor(String basePackage) {
        this.basePackage = basePackage;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isEmpty(includeFilters)) {
            addIncludeFilter(new IncludeAllFilter());
        }
        super.afterPropertiesSet();
    }

    /**
     * {@inheritDoc}
     */
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    /**
     * Set the ResourceLoader to use for resource locations.
     * This will typically be a ResourcePatternResolver implementation.
     * <p>Default is PathMatchingResourcePatternResolver, also capable of
     * resource pattern resolving through the ResourcePatternResolver interface.
     * @see org.springframework.core.io.support.ResourcePatternResolver
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
    }
    
    /**
     * Return the ResourceLoader used in locating matching resources.
     */
    public final ResourceLoader getResourceLoader() {
        return this.resourcePatternResolver;
    }

    /**
     * Set the resource pattern to use when scanning the classpath.
     * This value will be appended to each base package name.
     * @see #findTypesToRegister()
     * @see #DEFAULT_RESOURCE_PATTERN
     */
    public void setResourcePattern(String resourcePattern) {
        Assert.notNull(resourcePattern, "'resourcePattern' must not be null");
        this.resourcePattern = resourcePattern;
    }
    
    /**
     * Sets the list of type filters to use for inclusion.
     */
    public void setIncludeFilters(List<TypeFilter> includeFilters) {
        this.includeFilters = includeFilters;
    }

    /**
     * Sets the list of type filters to use for exclusion.
     */
    public void setExcludeFilters(List<TypeFilter> excludeFilters) {
        this.excludeFilters = excludeFilters;
    }

    /**
     * Add an include type filter to the <i>end</i> of the inclusion list.
     */
    public void addIncludeFilter(TypeFilter includeFilter) {
        this.includeFilters.add(includeFilter);
    }
    
    /**
     * Add an exclude type filter to the <i>front</i> of the exclusion list.
     */
    public void addExcludeFilter(TypeFilter excludeFilter) {
        this.excludeFilters.add(0, excludeFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<Class<?>> findTypesToRegister() {
        Set<Class<?>> typesToRegister = new HashSet<Class<?>>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + "/" + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            boolean traceEnabled = log.isTraceEnabled();
            boolean debugEnabled = log.isDebugEnabled();
            for (Resource resource : resources) {
                if (traceEnabled) {
                    log.trace("Scanning " + resource);
                }
                if (resource.isReadable()) {
                    try {
                        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                        if (applyFilters(metadataReader)) {
                            if (isCandidateForAmf(metadataReader.getAnnotationMetadata())) {
                                if (debugEnabled) {
                                    log.debug("Identified candidate AMF class: " + resource);
                                }
                                typesToRegister.add(ClassUtils.forName(metadataReader.getAnnotationMetadata().getClassName(), this.beanClassLoader));
                            }
                            else {
                                if (debugEnabled) {
                                    log.debug("Ignored because not a concrete top-level class: " + resource);
                                }
                            }
                        }
                        else {
                            if (traceEnabled) {
                                log.trace("Ignored because not matching any filter: " + resource);
                            }
                        }
                    }
                    catch (Throwable ex) {
                        throw new BeanDefinitionStoreException(
                                "Failed to read candidate AMF class: " + resource, ex);
                    }
                }
                else {
                    if (traceEnabled) {
                        log.trace("Ignored because not readable: " + resource);
                    }
                }
            }
        }
        catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return typesToRegister;
    }
    
    /**
     * Resolve the specified base package into a pattern specification for
     * the package search path.
     * <p>The default implementation resolves placeholders against system properties,
     * and converts a "."-based package path to a "/"-based resource path.
     * @param basePackage the base package as specified by the user
     * @return the pattern specification to be used for package searching
     */
    protected String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Determine whether the given class does not match any exclude filter
     * and does match at least one include filter.
     * @param metadataReader the ASM ClassReader for the class
     * @return whether the class passes the configured filters
     */
    protected boolean applyFilters(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine whether the given class qualifies for AMF conversion
     * <p>The default implementation checks whether the class is concrete
     * (i.e. not abstract and not an interface). Can be overridden in subclasses.
     * @param metadata the metadata for the class to check
     * @return whether the class qualifies for AMF conversion
     */
    protected boolean isCandidateForAmf(AnnotationMetadata metadata) {
        return (metadata.isConcrete() && metadata.isIndependent());
    }
    
    private final class IncludeAllFilter implements TypeFilter{
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            return isCandidateForAmf(metadataReader.getAnnotationMetadata());
        }
    }
}
