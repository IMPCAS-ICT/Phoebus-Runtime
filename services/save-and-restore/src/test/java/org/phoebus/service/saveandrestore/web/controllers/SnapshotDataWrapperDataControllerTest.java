/**
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.service.saveandrestore.web.controllers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.phoebus.service.saveandrestore.persistence.dao.NodeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.phoebus.applications.saveandrestore.model.ConfigPv;
import org.phoebus.applications.saveandrestore.model.Node;
import org.phoebus.applications.saveandrestore.model.NodeType;
import org.phoebus.applications.saveandrestore.model.SnapshotItem;
import org.phoebus.service.saveandrestore.SnapshotNotFoundException;
import org.phoebus.service.saveandrestore.web.config.ControllersTestConfig;

@ExtendWith(SpringExtension.class)
@ContextHierarchy({ @ContextConfiguration(classes = { ControllersTestConfig.class }) })
@WebMvcTest(SnapshotController.class)
public class SnapshotDataWrapperDataControllerTest {
	

	@Autowired
	private NodeDAO nodeDAO;

	@Autowired
	private MockMvc mockMvc;

	private static Node config1;

	private static Node snapshot;


	private ObjectMapper objectMapper = new ObjectMapper();

	private static final String JSON = "application/json;charset=UTF-8";
	
	@BeforeAll
	public static void setUp() {

		config1 = Node.builder()
				.uniqueId("1")
				.nodeType(NodeType.CONFIGURATION)
				.uniqueId("a")
				.build();

		snapshot = Node.builder()
				.nodeType(NodeType.SNAPSHOT)
				.build();
		
		snapshot.setUniqueId("7");

	}
	
	@Test
	public void testGetSnapshot() throws Exception{
		
		Mockito.reset(nodeDAO);
		
		when(nodeDAO.getSnapshotNode("b")).thenReturn(snapshot);
		
		MockHttpServletRequestBuilder request = get("/snapshot/b");

		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(JSON))
				.andReturn();
		
		// Make sure response contains expected data
		objectMapper.readValue(result.getResponse().getContentAsString(), Node.class);
	}
	
	@Test
	public void testGetConfigForSnapshot() throws Exception{
		
		when(nodeDAO.getParentNode("b")).thenReturn(config1);
		
		MockHttpServletRequestBuilder request = get("/node/b/parent");

		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(JSON))
				.andReturn();
		
		// Make sure response contains expected data
		objectMapper.readValue(result.getResponse().getContentAsString(), Node.class);
	}
	
	@Test
	public void testGetNonExistingSnapshot() throws Exception{
		
		when(nodeDAO.getSnapshotNode("c")).thenThrow(new SnapshotNotFoundException("askdmdsf"));
		
		MockHttpServletRequestBuilder request = get("/snapshot/c");

		mockMvc.perform(request).andExpect(status().isNotFound());
	}
	
	@Test
	public void testNonExistingSnapshot() throws Exception{
		when(nodeDAO.getSnapshotNode("x")).thenThrow(new SnapshotNotFoundException("lasdfk"));

		MockHttpServletRequestBuilder request = get("/snapshot/x").contentType(JSON);

		mockMvc.perform(request).andExpect(status().isNotFound());
	}
	
	
	@Test
	public void testGetSnapshotItems() throws Exception{
		
		SnapshotItem si = SnapshotItem.builder()
				.configPv(ConfigPv.builder().pvName("pvName").build())
				.build();
		
		when(nodeDAO.getSnapshotItems("si")).thenReturn(Arrays.asList(si));
		
		MockHttpServletRequestBuilder request = get("/snapshot/si/items");
			
		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(JSON))
				.andReturn();
		
		// Make sure response contains expected data
		objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<SnapshotItem>>() {
		});
	}
	
	@Test
	public void testSaveSnapshotBadRequest() throws Exception{
		MockHttpServletRequestBuilder request = put("/snapshot/configid").param("snapshotName", "a").param("comment", "c").param("userName", "u");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		List<SnapshotItem> snapshotItems = Arrays.asList(SnapshotItem.builder().build());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems))
				.param("snapshotName", "a").param("comment", "c");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems))
				.param("snapshotName", "a").param("userName", "c");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems))
				.param("userName", "a").param("comment", "c");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems)).contentType("application/json")
				.param("snapshotName", "").param("comment", "c").param("userName", "c");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems)).contentType("application/json")
				.param("snapshotName", "a").param("comment", "").param("userName", "c");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		request = put("/snapshot/configid")
				.content(objectMapper.writeValueAsString(snapshotItems)).contentType("application/json")
				.param("snapshotName", "a").param("comment", "c").param("userName", "");
		mockMvc.perform(request).andExpect(status().isBadRequest());
	}

	@Test
	public void testSaveSnapshot() throws Exception{
		
		Mockito.reset(nodeDAO);
		
		List<SnapshotItem> snapshotItems = Arrays.asList(SnapshotItem.builder().build());

		when(nodeDAO.saveSnapshot(Mockito.anyString(), Mockito.argThat(new ArgumentMatcher<List<SnapshotItem>>() {
			@Override
			public boolean matches(List<SnapshotItem> o) {
				return true;
			}
		}), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Node.builder().build());
		
		MockHttpServletRequestBuilder request = put("/snapshot/configid").param("snapshotName", "a").param("comment", "c").param("userName", "u");
		mockMvc.perform(request).andExpect(status().isBadRequest());
		
		
		request = put("/snapshot/configid")
				.contentType(JSON)
				.content(objectMapper.writeValueAsString(snapshotItems))
				.param("snapshotName", "a").param("comment", "c").param("userName", "u");
		
		MvcResult result = mockMvc.perform(request).andExpect(status().isOk()).andExpect(content().contentType(JSON))
				.andReturn();
		
		// Make sure response contains expected data
		objectMapper.readValue(result.getResponse().getContentAsString(), Node.class);
	}	
}
