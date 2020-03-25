package com.jagrosh.jdautilities.statuspage.tests;

import com.jagrosh.jdautilities.statuspage.StatusPage;
import org.junit.Test;

public class StatusPageTest
{
    public final StatusPage statusPage = new StatusPage();

    @Test
    public void testComponents() throws Exception
    {
        statusPage.getComponents().get();
    }

    @Test
    public void testIncidentsAll() throws Exception
    {
        statusPage.getIncidentsAll().get();
    }

    @Test
    public void testIncidentsUnresolved() throws Exception
    {
        statusPage.getIncidentsUnresolved().get();
    }

    @Test
    public void testScheduledMaintenancesActive() throws Exception
    {
        statusPage.getScheduledMaintenancesActive().get();
    }

    @Test
    public void testScheduledMaintenancesAll() throws Exception
    {
        statusPage.getScheduledMaintenancesAll().get();
    }

    @Test
    public void testScheduledMaintenancesUpcoming() throws Exception
    {
        statusPage.getScheduledMaintenancesUpcoming().get();
    }

    @Test
    public void testServiceStatus() throws Exception
    {
        statusPage.getServiceStatus().get();
    }

    @Test
    public void testSummary() throws Exception
    {
        statusPage.getSummary().get();
    }
}
