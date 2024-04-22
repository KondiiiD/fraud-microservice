package pl.kondi.customer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kondi.amqp.RabbitMQMessageProducer;
import pl.kondi.clients.fraud.FraudCheckResponse;
import pl.kondi.clients.fraud.FraudClient;
import pl.kondi.clients.notification.NotificationClient;
import pl.kondi.clients.notification.NotificationRequest;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;

    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        customerRepository.saveAndFlush(customer);

        FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        NotificationRequest notificationRequest = new NotificationRequest(customer.getId(),
                customer.getEmail(),
                "Hi %s, welcome to app".formatted(customer.getFirstName()));

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
                );
    }
}
