package com.sequenceiq.cloudbreak.shell.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.converters.AvailableCommandsConverter;
import org.springframework.shell.converters.BigDecimalConverter;
import org.springframework.shell.converters.BigIntegerConverter;
import org.springframework.shell.converters.BooleanConverter;
import org.springframework.shell.converters.CharacterConverter;
import org.springframework.shell.converters.DateConverter;
import org.springframework.shell.converters.DoubleConverter;
import org.springframework.shell.converters.EnumConverter;
import org.springframework.shell.converters.FloatConverter;
import org.springframework.shell.converters.IntegerConverter;
import org.springframework.shell.converters.LocaleConverter;
import org.springframework.shell.converters.LongConverter;
import org.springframework.shell.converters.ShortConverter;
import org.springframework.shell.converters.SimpleFileConverter;
import org.springframework.shell.converters.StaticFieldConverterImpl;
import org.springframework.shell.converters.StringConverter;
import org.springframework.shell.core.Converter;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.converter.HostGroupConverter;
import com.sequenceiq.cloudbreak.shell.converter.InstanceGroupConverter;
import com.sequenceiq.cloudbreak.shell.converter.InstanceGroupTemplateIdConverter;
import com.sequenceiq.cloudbreak.shell.converter.InstanceGroupTemplateNameConverter;
import com.sequenceiq.cloudbreak.shell.converter.NetworkIdConverter;
import com.sequenceiq.cloudbreak.shell.converter.NetworkNameConverter;
import com.sequenceiq.cloudbreak.shell.converter.StackRegionConverter;

/**
 * Configures the converters used by the shell.
 */
@Configuration
public class ConverterConfiguration {

    @Autowired
    private CloudbreakClient client;

    @Bean
    Converter simpleFileConverter() {
        return new SimpleFileConverter();
    }

    @Bean
    Converter stringConverter() {
        return new StringConverter();
    }

    @Bean
    Converter availableCommandsConverter() {
        return new AvailableCommandsConverter();
    }

    @Bean
    Converter bigDecimalConverter() {
        return new BigDecimalConverter();
    }

    @Bean
    Converter bigIntegerConverter() {
        return new BigIntegerConverter();
    }

    @Bean
    Converter booleanConverter() {
        return new BooleanConverter();
    }

    @Bean
    Converter characterConverter() {
        return new CharacterConverter();
    }

    @Bean
    Converter dateConverter() {
        return new DateConverter();
    }

    @Bean
    Converter doubleConverter() {
        return new DoubleConverter();
    }

    @Bean
    Converter enumConverter() {
        return new EnumConverter();
    }

    @Bean
    Converter floatConverter() {
        return new FloatConverter();
    }

    @Bean
    Converter integerConverter() {
        return new IntegerConverter();
    }

    @Bean
    Converter localeConverter() {
        return new LocaleConverter();
    }

    @Bean
    Converter longConverter() {
        return new LongConverter();
    }

    @Bean
    Converter shortConverter() {
        return new ShortConverter();
    }

    @Bean
    Converter hostGroupConverter() {
        return new HostGroupConverter(client);
    }

    @Bean
    Converter instanceGroupConverter() {
        return new InstanceGroupConverter(client);
    }

    @Bean
    Converter templateIdConverter() {
        return new InstanceGroupTemplateIdConverter(client);
    }

    @Bean
    Converter templateNameConverter() {
        return new InstanceGroupTemplateNameConverter(client);
    }

    @Bean
    Converter stackRegionConverter() {
        return new StackRegionConverter(client);
    }

    @Bean
    Converter staticFieldConverterImpl() {
        return new StaticFieldConverterImpl();
    }

    @Bean
    Converter networkIdConverter() {
        return new NetworkIdConverter(client);
    }

    @Bean
    Converter networkNameConverter() {
        return new NetworkNameConverter(client);
    }
}
