package com.codecentric.hystrix.warstories.service;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.codecentric.hystrix.warstories.entities.Customer;
import com.codecentric.hystrix.warstories.mapper.CustomerMapper;
import com.codecentric.hystrix.warstories.repository.CustomerRepository;
import com.codecentric.hystrix.warstories.shared.dto.CustomerDTO;
import com.codecentric.hystrix.warstories.shared.utils.ChaosMonkey;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * @author Benjamin Wilms (xd98870)
 */

@Service
public class CustomerService {

    private static final Log LOGGER = LogFactory.getLog(CustomerService.class);

    private CustomerRepository customerRepository;

    private ChaosMonkey chaosMonkey;

    private ModelMapper modelMapper;

    private HashMap<Long, CustomerDTO> fallbackCache = new HashMap<>();

    // use simulated legacy system?
    private DynamicBooleanProperty chaosMonkeyActive =
        DynamicPropertyFactory.getInstance().getBooleanProperty("chaos.monkey.active", false);

    @Autowired
    public CustomerService(CustomerRepository customerRepository, ChaosMonkey chaosMonkey) {
        this.customerRepository = customerRepository;
        this.customerRepository = customerRepository;
        this.chaosMonkey = chaosMonkey;

        // Init ModelMapper
        modelMapper = new ModelMapper();
        modelMapper.addMappings(new CustomerMapper());

        // init fallback cache used by hystrix fallback
        initFallbackCache();
    }

    private void initFallbackCache() {

        LOGGER.debug(LOGGER.isDebugEnabled() ? "Init FallbackCache" : null);

        List<Customer> customerList = customerRepository.findAll();

        for (Customer customer : customerList) {
            CustomerDTO dto = modelMapper.map(customer, CustomerDTO.class);

            // marked as cached object
            dto.setCached(true);

            fallbackCache.put(dto.getAccountNumber(), dto);
        }

        LOGGER.debug(LOGGER.isDebugEnabled() ? "FallbackCache filled up: " + fallbackCache.size() : null);

    }

    @HystrixCommand(fallbackMethod = "fallbackCache", commandKey = "CustomerServiceKey")
    public CustomerDTO findCustomerByAccountNumber(long accountNumber) {

        Customer customer;

        if (chaosMonkeyActive.get()) {

            LOGGER.debug(LOGGER.isDebugEnabled() ? "Chaos Monkey is active" : null);
            // call the monkey to get in trouble
            chaosMonkey.iWantTrouble();

            // Loading Data
            customer = customerRepository.findByAccountNumber(accountNumber);

        } else {
            LOGGER.debug(LOGGER.isDebugEnabled() ? "Chaos Monkey is inactive" : null);

            // Default loading without trouble
            customer = customerRepository.findByAccountNumber(accountNumber);
        }
        return customer == null ? null : modelMapper.map(customer, CustomerDTO.class);

    }

    /***
     * Fallback Cache used by Hstrix
     * @param accountNumber
     * @return
     */
    private CustomerDTO fallbackCache(long accountNumber) {

        return fallbackCache.get(accountNumber);

    }
}
