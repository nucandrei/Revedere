package org.nuc.revedere.service.core;

public class SupervisorTopics {

    // Supervisor information related topic
    public static final String HEARTBEAT_TOPIC = "Revedere.Supervised.Heartbeat";
    
    public static final String COMMAND_TOPIC = "Revedere.Supervisor.Command";
    
    public static final String PUBLISH_TOPIC = "Revedere.Supervisor.Publish";
    
    private SupervisorTopics() {
     // empty constructor. It protects from creating instances of SupervisorTopics
    }
}
