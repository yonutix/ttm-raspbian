package tatami.core.control;

import Logging.Log;
import tatami.core.agent.AgentComponent;
import tatami.core.agent.AgentComponent.AgentComponentName;
import tatami.core.agent.AgentEvent;
import tatami.core.agent.CompositeAgent;
import tatami.core.agent.messaging.MessagingComponent;

/**
 * Created by yonutix on 06/05/2016.
 */
public class ControlComponent extends AgentComponent {
    public static enum Vocabulary {
        /**
         * Posts an event to the agent's event queue, as requested by the simulation agent.
         */
        CONTROL,
    }

    public ControlComponent()
    {
        super(AgentComponentName.CONTROL_COMPONENT);
        Log.v("smth", "Control component instantiated");
    }

    @Override
    protected void parentChangeNotifier(CompositeAgent oldParent) {
        super.parentChangeNotifier(oldParent);
    }

    @Override
    protected void atAgentStart(AgentEvent event)
    {
        super.atAgentStart(event);
        registerMessageHandlers();
    }

    @Override
    protected void atAgentStop(AgentEvent event)
    {
        super.atAgentStop(event);
    }

    protected void atBeforeAgentMove(AgentEvent event)
    {
    }

    protected void atAfterAgentMove(AgentEvent event)
    {
    }

    /**
     * Registers the message handlers with the messaging component of the agent.
     */
    protected void registerMessageHandlers()
    {

        registerMessageReceiver(new AgentEvent.AgentEventHandler() {
            private static final long serialVersionUID = -6379292023931191463L;

            @Override
            public void handleEvent(AgentEvent event)
            {
                String content = event.get(MessagingComponent.CONTENT_PARAMETER);
                postAgentEvent(new AgentEvent(AgentEvent.AgentEventType.valueOf(content)));
            }
        }, Vocabulary.CONTROL.toString(), Vocabulary.CONTROL.toString());
    }

    @Override
    protected AgentComponent getAgentComponent(AgentComponentName name)
    {
        return super.getAgentComponent(name);
    }

    @Override
    protected void postAgentEvent(AgentEvent event)
    {
        super.postAgentEvent(event);
    }

    protected void atAgentResume(AgentEvent event) {
        registerMessageHandlers();
    }
}
