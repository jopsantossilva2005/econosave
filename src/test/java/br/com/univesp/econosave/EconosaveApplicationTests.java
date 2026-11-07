package br.com.univesp.econosave;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:econosave-test", "spring.datasource.username=sa", "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureMockMvc
class EconosaveApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveExecutarFluxoCompletoDeAutenticacaoECrudDeMovimentacoes() throws Exception {
		String emailUnico = "teste." + UUID.randomUUID() + "@econosave.com";

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"João Teste\",\"email\":\"" + emailUnico + "\",\"senha\":\"12345678\"}"))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/auth/register")
						.header("X-Requested-With", "EconoSave")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nome\":\"João Teste\",\"email\":\"" + emailUnico + "\",\"senha\":\"12345678\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value(emailUnico));

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.header("X-Requested-With", "EconoSave")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + emailUnico + "\",\"senha\":\"12345678\"}"))
				.andExpect(status().isOk())
				.andReturn();

		MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

		mockMvc.perform(get("/api/categories").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(8));

		mockMvc.perform(post("/api/transactions")
						.session(session)
						.header("X-Requested-With", "EconoSave")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"descricao\":\"Mercado Semanal\",\"valor\":250.50,\"tipo\":\"DESPESA\",\"data\":\"2026-10-15\",\"categoriaId\":1}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.descricao").value("Mercado Semanal"));

		mockMvc.perform(get("/api/transactions").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(post("/api/auth/logout")
						.session(session)
						.header("X-Requested-With", "EconoSave"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/transactions").session(session))
				.andExpect(status().isUnauthorized());
	}
}