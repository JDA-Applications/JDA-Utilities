package com.jagrosh.jdautilities.statuspage.tests;

import com.jagrosh.jdautilities.statuspage.Statuspage;
import org.junit.Test;

public class StatuspageTest
{
    public final Statuspage statuspage = new Statuspage();

    @Test
    public void testComponents() throws Exception
    {
        statuspage.getComponents().get();
    }

    @Test
    public void testIncidentsAll() throws Exception
    {
        statuspage.getIncidentsAll().get();
    }

    @Test
    public void testIncidentsUnresolved() throws Exception
    {
        statuspage.getIncidentsUnresolved().get();
    }

    @Test
    public void testScheduledMaintenancesActive() throws Exception
    {
        statuspage.getScheduledMaintenancesActive().get();
    }

    @Test
    public void testScheduledMaintenancesAll() throws Exception
    {
        statuspage.getScheduledMaintenancesAll().get();
    }

    @Test
    public void testScheduledMaintenancesUpcoming() throws Exception
    {
        statuspage.getScheduledMaintenancesUpcoming().get();
    }

    @Test
    public void testServiceStatus() throws Exception
    {
        statuspage.getServiceStatus().get();
    }

    @Test
    public void testSummary() throws Exception
    {
        statuspage.getSummary().get();
    }
}
