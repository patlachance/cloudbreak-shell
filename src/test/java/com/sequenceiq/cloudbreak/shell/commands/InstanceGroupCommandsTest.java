package com.sequenceiq.cloudbreak.shell.commands;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Matchers.any;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroup;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateId;
import com.sequenceiq.cloudbreak.shell.completion.InstanceGroupTemplateName;
import com.sequenceiq.cloudbreak.shell.model.CloudbreakContext;
import com.sequenceiq.cloudbreak.shell.model.InstanceGroupEntry;

public class InstanceGroupCommandsTest {

    private static final Integer DUMMY_NODE_COUNT = 1;
    private static final String DUMMY_TEMPLATE = "dummy-template";
    private static final String DUMMY_TEMPLATE_ID = "50";
    private InstanceGroup hostGroup = new InstanceGroup("master");
    private InstanceGroupTemplateId dummyTemplateId = new InstanceGroupTemplateId(DUMMY_TEMPLATE_ID);
    private InstanceGroupTemplateName dummyTemplateName = new InstanceGroupTemplateName(DUMMY_TEMPLATE);

    @InjectMocks
    private InstanceGroupCommands underTest;

    @Mock
    private CloudbreakClient mockClient;

    @Mock
    private CloudbreakContext mockContext;

    private Map<String, Object> dummyResult;

    @Before
    public void setUp() {
        underTest = new InstanceGroupCommands();
        hostGroup = new InstanceGroup("master");
        MockitoAnnotations.initMocks(this);
        dummyResult = new HashMap<>();
        dummyResult.put("id", DUMMY_TEMPLATE_ID);
    }

    @Test
    public void testConfigureByTemplateId() throws Exception {
        underTest.createInstanceGroup(hostGroup, DUMMY_NODE_COUNT, dummyTemplateId, null);
        verify(mockContext, times(1)).putInstanceGroup(anyString(), any(InstanceGroupEntry.class));
    }

    @Test
    public void testConfigureByTemplateName() throws Exception {
        given(mockClient.getTemplateByName(DUMMY_TEMPLATE)).willReturn(dummyResult);
        underTest.createInstanceGroup(hostGroup, DUMMY_NODE_COUNT, null, dummyTemplateName);
        verify(mockClient, times(1)).getTemplateByName(anyString());
        verify(mockContext, times(1)).putInstanceGroup(anyString(), any(InstanceGroupEntry.class));
    }

    @Test
    public void testConfigureByTemplateIdAndName() throws Exception {
        underTest.createInstanceGroup(hostGroup, DUMMY_NODE_COUNT, dummyTemplateId, dummyTemplateName);
        verify(mockContext, times(1)).putInstanceGroup(anyString(), any(InstanceGroupEntry.class));
        verify(mockClient, times(0)).getTemplateByName(anyString());
    }

    @Test
    public void testConfigureByTemplateNameWhenTemplateNotFound() throws Exception {
        given(mockClient.getTemplateByName(DUMMY_TEMPLATE)).willReturn(null);
        underTest.createInstanceGroup(hostGroup, DUMMY_NODE_COUNT, null, dummyTemplateName);
        verify(mockClient, times(1)).getTemplateByName(anyString());
        verify(mockContext, times(0)).putInstanceGroup(anyString(), any(InstanceGroupEntry.class));
    }
}
