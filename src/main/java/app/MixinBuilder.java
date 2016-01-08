package app;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MixinBuilder {
    private static Object invoke(Method method, Object target, Object[] args)
            throws Exception {
        return target.getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .invoke(target, args);
    }

    @SuppressWarnings("unchecked")
    private static <T> T newMixinInstance(final Object parent,
                                          final Class<T> mixinImpl) {
        try {
            final ProxyFactory proxy = new ProxyFactory();
            proxy.setSuperclass(mixinImpl);
            return (T) proxy.create(null, null, new MethodHandler() {
                public Object invoke(Object target, Method method,
                                     Method superMethod, Object[] args) throws Throwable {
                    // Delegate invocations of abstract methods
                    // to parent object.
                    if (Modifier.isAbstract(method.getModifiers())) {
                        return MixinBuilder.invoke(method, parent, args);
                    }
                    return superMethod.invoke(target, args);
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(final Class<T> mainClass) {
        try {
            final ProxyFactory proxy = new ProxyFactory();
            proxy.setSuperclass(mainClass);
            return (T) proxy.create(null, null, new MethodHandler() {
                private Map<Class<?>, Object> mixinMap = new HashMap<Class<?>, Object>();

                public Object invoke(Object target, Method method,
                                     Method superMethod, Object[] args) throws Throwable {
                    // Delegate invocations of abstract methods
                    // to mixin.
                    if (Modifier.isAbstract(method.getModifiers())) {
                        Class<?> mixinInterface = method.getDeclaringClass();
                        if (!mixinMap.containsKey(mixinInterface)) {
                            Mixin annotation = mixinInterface
                                    .getAnnotation(Mixin.class);
                            final Class<?> mixinClass = annotation.impl();
                            mixinMap.put(mixinInterface,
                                    newMixinInstance(target, mixinClass));
                        }
                        Object mixin = mixinMap.get(mixinInterface);
                        return MixinBuilder.invoke(method, mixin, args);
                    }
                    return superMethod.invoke(target, args);
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
