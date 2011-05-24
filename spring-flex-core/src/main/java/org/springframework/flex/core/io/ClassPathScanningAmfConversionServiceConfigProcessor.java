package org.springframework.flex.core.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SystemPropertyUtils;


public class ClassPathScanningAmfConversionServiceConfigProcessor extends AbstractAmfConversionServiceConfigProcessor implements ResourceLoaderAware, BeanClassLoaderAware {

    private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    protected final Log logger = LogFactory.getLog(getClass());

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    private String resourcePattern = DEFAULT_RESOURCE_PATTERN;
    
    private ClassLoader beanClassLoader;

    private List<TypeFilter> includeFilters = new LinkedList<TypeFilter>();

    private List<TypeFilter> excludeFilters = new LinkedList<TypeFilter>();
    
    private final String basePackage;
    
    public ClassPathScanningAmfConversionServiceConfigProcessor(String basePackage) {
        this.basePackage = basePackage;
    }
    
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
     * Return the ResourceLoader that this scanner uses.
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
    
    public void setIncludeFilters(List<TypeFilter> includeFilters) {
        this.includeFilters = includeFilters;
    }

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

    @Override
    protected Set<Class<?>> findTypesToRegister() {
        Set<Class<?>> typesToRegister = new HashSet<Class<?>>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    resolveBasePackage(basePackage) + "/" + this.resourcePattern;
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            boolean traceEnabled = logger.isTraceEnabled();
            boolean debugEnabled = logger.isDebugEnabled();
            for (Resource resource : resources) {
                if (traceEnabled) {
                    logger.trace("Scanning " + resource);
                }
                if (resource.isReadable()) {
                    try {
                        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                        if (applyFilters(metadataReader)) {
                            if (isCandidateForAmf(metadataReader.getAnnotationMetadata())) {
                                if (debugEnabled) {
                                    logger.debug("Identified candidate AMF class: " + resource);
                                }
                                typesToRegister.add(ClassUtils.forName(metadataReader.getAnnotationMetadata().getClassName(), this.beanClassLoader));
                            }
                            else {
                                if (debugEnabled) {
                                    logger.debug("Ignored because not a concrete top-level class: " + resource);
                                }
                            }
                        }
                        else {
                            if (traceEnabled) {
                                logger.trace("Ignored because not matching any filter: " + resource);
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
                        logger.trace("Ignored because not readable: " + resource);
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
