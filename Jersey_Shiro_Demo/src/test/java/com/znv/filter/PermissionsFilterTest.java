package com.znv.filter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import javax.ws.rs.WebApplicationException;

import com.znv.filter.PermissionsFilter;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;

import com.sun.jersey.spi.container.ContainerRequest;
import org.junit.Test;

public class PermissionsFilterTest {

    @Test
    public void constructor() {
        PermissionsFilter filter = new PermissionsFilter("repository:read");
        assertNull(filter.getResponseFilter());
        assertEquals(filter, filter.getRequestFilter());
        assertNotNull(filter.getRequiredPermissions());
        assertEquals(1, filter.getRequiredPermissions().length);
    }

    @Test
    public void emptyPermissions() {
        PermissionsFilter filter = new PermissionsFilter();
        assertNotNull(filter.getRequiredPermissions());
        assertEquals(0, filter.getRequiredPermissions().length);
    }

    @Test
    public void filterWithPermission() {
        PermissionsFilter filter = mock(PermissionsFilter.class);
        doCallRealMethod().when(filter).filter(any(ContainerRequest.class));
        when(filter.checkConditions()).thenReturn(true);

        ContainerRequest request = mock(ContainerRequest.class);
        assertEquals(request, filter.filter(request));
    }

    @Test(expected = WebApplicationException.class)
    public void filterWithoutPermission() {
        PermissionsFilter filter = mock(PermissionsFilter.class);
        doCallRealMethod().when(filter).filter(any(ContainerRequest.class));
        when(filter.checkConditions()).thenReturn(false);

        ContainerRequest request = mock(ContainerRequest.class);
        assertEquals(request, filter.filter(request));
    }

    @Test
    public void checkConditions() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                PermissionsFilter filter = new PermissionsFilter("repository:read");

                Subject subject = mock(Subject.class);
                SubjectThreadState threadState = new SubjectThreadState(subject);
                threadState.bind();

                assertFalse(PermissionsFilter.checkConditions("repository:write"));
                assertFalse(filter.checkConditions());
            }
        };

        runnable.run();
    }

}
