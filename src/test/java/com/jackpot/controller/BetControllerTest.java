package com.jackpot.controller;

import com.jackpot.dto.BetRequestDto;
import com.jackpot.service.BetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetControllerTest {

    @Mock
    private BetService betService;

    @InjectMocks
    private BetController betController;

    @Test
    void publishBet_Success() {
        BetRequestDto request = new BetRequestDto();
        request.setBetId("bet-1");

        doNothing().when(betService).processBet(request);
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = betController.publishBet(request, mockBindingResult);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Bet published successfully", response.getBody());
        verify(betService, times(1)).processBet(request);
    }

    @Test
    void publishBet_IllegalArgumentException() {
        BetRequestDto request = new BetRequestDto();
        doThrow(new IllegalArgumentException("Invalid bet")).when(betService).processBet(request);
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = betController.publishBet(request, mockBindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid bet", response.getBody());
    }

    @Test
    void publishBet_UnexpectedException() {
        BetRequestDto request = new BetRequestDto();
        doThrow(new RuntimeException("Kafka down")).when(betService).processBet(request);
        BindingResult mockBindingResult = mock(BindingResult.class);
        when(mockBindingResult.hasErrors()).thenReturn(false);

        ResponseEntity<?> response = betController.publishBet(request, mockBindingResult);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody());
    }
}
