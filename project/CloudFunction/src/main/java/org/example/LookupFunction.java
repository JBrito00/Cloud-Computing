package org.example;

import com.google.cloud.compute.v1.*;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.functions.HttpFunction;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cloud Function that retrieves IP addresses of VM instances from a specified instance group in Google Cloud Platform.
 */
public class LookupFunction implements HttpFunction {

    /**
     * Handles HTTP requests and retrieves IP addresses of VM instances from a specified instance group.
     *
     * @param request  the HTTP request containing query parameters.
     * @param response the HTTP response to write the result to.
     * @throws Exception if any error occurs during processing.
     */
    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String zone = request.getFirstQueryParameter("zone").orElse("");
        String project = request.getFirstQueryParameter("prjid").orElse("");
        String group = request.getFirstQueryParameter("group").orElse("");

        if (zone.isEmpty() || project.isEmpty() || group.isEmpty()) {
            response.setStatusCode(400);
            response.getWriter().write("{\"error\": \"Missing required query parameters\"}");
            return;
        }

        try {
            List<String> ips = getInstanceGroupIps(zone, project, group);
            response.setStatusCode(200);
            response.getWriter().write(new Gson().toJson(ips));
        } catch (IOException e) {
            response.setStatusCode(500);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Retrieves the IP addresses of VM instances in the specified instance group.
     *
     * @param zone    the zone of the instance group.
     * @param project the project ID in Google Cloud Platform.
     * @param group   the name of the instance group.
     * @return a list of IP addresses of the VM instances.
     * @throws IOException if any error occurs while fetching the IP addresses.
     */
    private List<String> getInstanceGroupIps(String zone, String project, String group) throws IOException {
        List<String> ips = new ArrayList<>();
        System.out.println("==== Listing IPs of running VM from na instance group: " + group);
        try (InstancesClient client = InstancesClient.create()) {
            for (Instance curInst : client.list(project, zone).iterateAll()) {
                if (curInst.getName().contains(group)) {
                    String ip = curInst.getNetworkInterfaces(0).getAccessConfigs(0).getNatIP();
                    ips.add(ip);
                }
            }
        }
        return ips;
    }

}