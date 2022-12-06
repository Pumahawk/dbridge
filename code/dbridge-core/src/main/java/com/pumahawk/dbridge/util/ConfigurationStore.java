package com.pumahawk.dbridge.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pumahawk.dbridge.configuration.ConfigurationResource;
import com.pumahawk.dbridge.configuration.DBridgeConfigResource;
import com.pumahawk.dbridge.configuration.Database;
import com.pumahawk.dbridge.configuration.GlobalValidator;
import com.pumahawk.dbridge.configuration.GlobalValidatorResource;
import com.pumahawk.dbridge.configuration.GlobalValidatorSpec;
import com.pumahawk.dbridge.configuration.Kind;
import com.pumahawk.dbridge.configuration.QueryResource;
import com.pumahawk.dbridge.configuration.QuerySpec;
import com.pumahawk.dbridge.configuration.Validator;

public class ConfigurationStore {

    HashMap<Kind, Map<String, ConfigurationResource<? extends Object>>> store = new HashMap<>();

    public void add(ConfigurationResource<? extends Object> resource) {
        Map<String, ConfigurationResource<? extends Object>> rs = get(resource.getKind());
        rs.put(resource.getMetadata().getName(), resource);
    }

    public Map<String, ConfigurationResource<? extends Object>> get(Kind key) {
        Map<String, ConfigurationResource<? extends Object>> resource = store.get(key);
        if (resource == null) {
            resource = new HashMap<>();
            store.put(key, resource);
        }
        return resource;
    }

    public Stream<QueryResource> getQueries() {
        Stream<QueryResource> st = getStreamByType(Kind.QUERY);
        return st.peek(s -> Optional.of(s)
                .map(QueryResource::getSpec)
                .map(QuerySpec::getValidators)
                .ifPresent(vs -> resolveExtension(vs)));
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<T> getStreamByType(Kind kind) {
        return get(kind).values().stream().map(v -> (T) v); 

    }

    public Stream<GlobalValidatorResource> getValidators() {
        return getStreamByType(Kind.GLOBAL_VALIDATOR);
    }

    public Stream<DBridgeConfigResource> getDBridgeConfig() {
        return getStreamByType(Kind.DBRIDGE_CONFIG);
    }

    public Stream<Database> getDatabase() {
        return getDBridgeConfig().map(DBridgeConfigResource::getSpec).flatMap(s -> s.getDatabase().stream());
    }

    public Integer size() {
        return store.size();
    }

    private void resolveExtension(List<Validator> vs) {
        List<GlobalValidator> globalValidators = getValidators()
            .map(GlobalValidatorResource::getSpec)
            .map(GlobalValidatorSpec::getGlobalValidators)
            .flatMap(gvs -> gvs.stream())
            .collect(Collectors.toList());
        List<String> extended = new LinkedList<>();
        resolveExtension(vs, globalValidators, extended);
    }

    private void resolveExtension(List<Validator> vs, List<GlobalValidator> globalValidators, List<String> extended) {
        List<Validator> extendedValidators = new LinkedList<>();
        vs.forEach(v -> extendedValidators.add(cloneValidator(v)));
        for (int i = 0; i < extendedValidators.size(); i++) {
            Validator v = extendedValidators.get(i);
            String extend = v.getExtends();
            if (extend != null && !extended.contains(extend)) {
                v.setExtends(null);
                extended.add(extend);
                List<Validator> nv = globalValidators.stream()
                    .filter(gv -> extend.equals(gv.getName()))
                    .findAny()
                    .map(GlobalValidator::getValidators)
                    .map(gv -> gv.stream().collect(() -> new LinkedList<Validator>(),
                        (l, v3) -> l.add(cloneValidator(v3)),
                        (l1, l2) -> l1.addAll(l2)))
                    .orElseGet(() -> new LinkedList<>());
                if (nv.size() > 0) {
                    extendedValidators.addAll(i--, nv);
                }
            }
        }
        vs.removeIf(v -> true);
        vs.addAll(extendedValidators);
    }

    private Validator cloneValidator(Validator validator) {
        return new ObjectMapper().convertValue(validator, Validator.class);
    }
}
