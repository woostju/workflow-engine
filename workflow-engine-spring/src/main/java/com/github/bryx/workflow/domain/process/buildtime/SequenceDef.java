package com.github.bryx.workflow.domain.process.buildtime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SequenceDef extends ProcessDefElement {

     String conditionExpression;
     String sourceRef;
     String targetRef;
     String skipExpression;
}
