package com.pos.dto;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;
import com.pos.service.ApiException;
import com.pos.service.ClientService;
import com.pos.utils.ClientConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
public class ClientDto extends AbstractDto {

    @Autowired
    private ClientService clientService;
    //TODO normalise me sirf form pass krna chahiye
    //TODO later - return client data while doing ui
    public void addClient(@Valid ClientForm clientForm) throws ApiException {
        validateForm(clientForm);
        //normalize(clientForm) --- should be like this, use reflections, template in the params
        //clientForm.setName(normalize(clientForm.getName()));
        //clientForm.setEmail(normalize(clientForm.getEmail()));

        ClientPojo clientPojo = ClientConversion.convertFormToPojo(clientForm);
        clientService.addClient(clientPojo);
    }
    //naming conventions
    public void update(Integer id, ClientForm clientForm) throws ApiException {
        validateForm(clientForm);
//        normalize(form)
//        clientForm.setName(normalize(clientForm.getName()));
//        clientForm.setEmail(normalize(clientForm.getEmail()));

        ClientPojo p = ClientConversion.convertFormToPojo(id, clientForm);
        clientService.update(id, p);
    }

    // UNIFIED METHOD: Decides between Search (Full List) and Pagination (Chunked List)
    public List<ClientData> getClients(Integer id, String name, String email, int page, int size) throws ApiException {
        List<ClientPojo> pojos;

//        if (isSearch(id, name, email)) {
//            // When searching, we typically return all matches without pagination
//            // to ensure the user finds exactly what they typed.
//            pojos = clientService.getFiltered(id, normalize(name), normalize(email));
//        } else {
//            // When no search is active, we return the specific page requested.
//            pojos = clientService.getPaged(page, size);
//        }

        pojos = clientService.getFiltered(id, normalize(name), normalize(email));
        return pojos.stream()
                .map(ClientConversion::convertFormToPojo)
                .toList();
    }
    //TODO make this private, public upper, private neeche
    public Long getTotalClients() {
        return clientService.getCount();
    }

    //TODO make this paginated
    public List<ClientData> getAll() {
        return clientService.getAll().stream()
                .map(ClientConversion::convertFormToPojo)
                .toList();
    }
//    // Logic Switch: Checks if any search parameters are actually filled
//    private boolean isSearch(Integer id, String n, String e) {
//        return id != null || (n != null && !n.isEmpty()) || (e != null && !e.isEmpty());
//    }
}