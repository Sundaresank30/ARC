package com.arc.embossing.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class EmbossingSimulationProperties {

    @Value("${embossing.simulation.in-machine-delay-ms:2000}")
    private long inMachineDelayMs;

    @Value("${embossing.simulation.printing-delay-ms:5000}")
    private long printingDelayMs;

    @Value("${embossing.simulation.active-batch:Batch_1}")
    private String activeBatch;

    @Value("${embossing.simulation.dummy-job-count:5}")
    private int dummyJobCount;
}
