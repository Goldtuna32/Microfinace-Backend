package com.sme.config;

import com.sme.dto.CIFDTO;
import com.sme.entity.CIF;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    private ModelMapper modelMapper;

    private void configureMappings() {
        // Map CIF entity to CIFDTO
        modelMapper.typeMap(CIF.class, CIFDTO.class).addMappings(mapper -> {
            mapper.map(CIF::getFNrcPhotoUrl, CIFDTO::setFNrcPhotoUrl);
            mapper.map(CIF::getBNrcPhotoUrl, CIFDTO::setBNrcPhotoUrl);
        });
    }
}
