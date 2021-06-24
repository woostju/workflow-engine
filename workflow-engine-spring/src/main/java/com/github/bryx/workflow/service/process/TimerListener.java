package com.github.bryx.workflow.service.process;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;


public class TimerListener implements ActivitiEventListener {
	private ProcessService processService;

	public TimerListener(ProcessService processService){
		this.processService = processService;
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		if(event.getType().equals(ActivitiEventType.TIMER_FIRED)){
			this.processService.timerEventTriggered(event);
		}
	}

	@Override
	public boolean isFailOnException() {
		return false;
	}

}
