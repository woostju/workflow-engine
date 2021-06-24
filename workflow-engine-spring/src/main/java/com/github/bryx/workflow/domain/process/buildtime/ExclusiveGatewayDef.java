package com.github.bryx.workflow.domain.process.buildtime;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExclusiveGatewayDef extends ProcessDefElement {
    List<SequenceDef> incomingFlows;
}
