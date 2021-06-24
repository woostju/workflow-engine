package com.github.bryx.workflow.domain.process.buildtime;

import lombok.Data;

@Data
public class ProcessDef {
	private String id;
	private String name;
	private String key;
	private int version;
	private String deploymentId;
}
