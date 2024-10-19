package com.tananushka.elastic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tananushka.elastic.dto.EmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

   private static final String INDEX_NAME = "employees";
   private static final RestClient restClient = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();

   private final ObjectMapper objectMapper;

   public String getAllEmployees(int page, int size) throws IOException {
      int from = page * size;

      Request request = new Request("GET", "/" + INDEX_NAME + "/_search");
      request.addParameter("from", String.valueOf(from));
      request.addParameter("size", String.valueOf(size));

      Response response = restClient.performRequest(request);
      return EntityUtils.toString(response.getEntity());
   }

   public String getEmployeeById(String id) throws IOException {
      Request request = new Request("GET", "/" + INDEX_NAME + "/_doc/" + id);
      Response response = restClient.performRequest(request);
      return EntityUtils.toString(response.getEntity());
   }

   public String createEmployee(String id, EmployeeDTO employee) throws IOException {
      employee.setId(id);
      String employeeJson = objectMapper.writeValueAsString(employee);
      Request request = new Request("PUT", "/" + INDEX_NAME + "/_doc/" + id);
      request.setJsonEntity(employeeJson);
      Response response = restClient.performRequest(request);
      return EntityUtils.toString(response.getEntity());
   }

   public String deleteEmployeeById(String id) throws IOException {
      Request request = new Request("DELETE", "/" + INDEX_NAME + "/_doc/" + id);
      Response response = restClient.performRequest(request);
      return EntityUtils.toString(response.getEntity());
   }

   public String searchEmployees(String field, String value, String queryType) throws IOException {
      log.debug("Starting search for field: {}, value: {}, queryType: {}", field, value, queryType);
      String queryJson;
      if ("match".equalsIgnoreCase(queryType)) {
         queryJson = String.format("{ \"query\": { \"match\": { \"%s\": \"%s\" } } }", field, value);
         log.debug("Executing Match Query: {}", queryJson);
      } else if ("term".equalsIgnoreCase(queryType)) {
         String keywordField = field.endsWith(".keyword") ? field : field + ".keyword";
         queryJson = String.format("{ \"query\": { \"term\": { \"%s\": \"%s\" } } }", keywordField, value);
         log.debug("Executing Term Query with potential keyword field: {}", queryJson);
      } else {
         log.error("Invalid query type: {}", queryType);
         throw new IllegalArgumentException("Invalid query type. Use 'match' or 'term'.");
      }

      log.debug("Sending request to Elasticsearch with the query: {}", queryJson);
      Request request = new Request("POST", "/" + INDEX_NAME + "/_search");
      request.setJsonEntity(queryJson);

      Response response = restClient.performRequest(request);
      String responseBody = EntityUtils.toString(response.getEntity());
      log.debug("Elasticsearch response: {}", responseBody);

      return responseBody;
   }

   public String aggregateEmployees(String field, String fieldValue, String metricType, String metricField) throws IOException {
      String aggName = metricType + "_" + metricField;

      String aggQuery = String.format("{ \"query\": { \"match\": { \"%s\": \"%s\" } }, \"aggs\": { \"%s\": { \"%s\": { \"field\": \"%s\" } } } }",
            field, fieldValue, aggName, metricType, metricField);

      log.debug("Executing Aggregation Query: {}", aggQuery);

      Request request = new Request("POST", "/" + INDEX_NAME + "/_search");
      request.setJsonEntity(aggQuery);

      Response response = restClient.performRequest(request);
      return EntityUtils.toString(response.getEntity());
   }
}