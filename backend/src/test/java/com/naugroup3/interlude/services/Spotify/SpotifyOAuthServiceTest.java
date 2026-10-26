package com.naugroup3.interlude.services.Spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;

class SpotifyOAuthServiceTest {
    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String REDIRECT_URI = "http://127.0.0.1:8082/auth/spotify/callback";

    private MockRestServiceServer mockServer;
    private SpotifyOAuthService service;

    @BeforeEach
    void setUp( ) {
        RestClient.Builder builder = RestClient.builder( );
        mockServer = MockRestServiceServer.bindTo( builder ).build( );
        service = new SpotifyOAuthService( CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, builder.build( ) );
    }

    @AfterEach
    void verifyRequests( ) {
        mockServer.verify( );
    }

    @Test
    void builds_authorization_url_with_required_query_parameters( ) {
        String authorizationUrl = service.buildAuthorizationUrl( "random-state" );
        var query = UriComponentsBuilder.fromUriString( authorizationUrl )
            .build( true )
            .getQueryParams( );

        assertThat( authorizationUrl ).startsWith( "https://accounts.spotify.com/authorize?" );
        assertThat( query.getFirst( "response_type" ) ).isEqualTo( "code" );
        assertThat( query.getFirst( "client_id" ) ).isEqualTo( CLIENT_ID );
        assertThat( query.getFirst( "redirect_uri" ) ).isEqualTo( REDIRECT_URI );
        assertThat( query.getFirst( "state" ) ).isEqualTo( "random-state" );
        assertThat( query.getFirst( "scope" ) )
            .contains( "user-read-currently-playing" )
            .contains( "user-read-recently-played" );
    }

    @Test
    void exchanges_authorization_code_for_tokens( ) {
        String basicCredentials = Base64.getEncoder()
            .encodeToString( ( CLIENT_ID + ":" + CLIENT_SECRET ).getBytes( StandardCharsets.UTF_8 ) );
        mockServer.expect( requestTo( "https://accounts.spotify.com/api/token" ) )
            .andExpect( method( HttpMethod.POST ) )
            .andExpect( header( HttpHeaders.AUTHORIZATION, "Basic " + basicCredentials ) )
            .andExpect( content( ).contentTypeCompatibleWith( MediaType.APPLICATION_FORM_URLENCODED ) )
            .andExpect( content( ).formDataContains( Map.of(
                "grant_type", "authorization_code",
                "code", "test-code",
                "redirect_uri", REDIRECT_URI ) ) )
            .andRespond( withSuccess( """
                {
                  "access_token": "access-token",
                  "token_type": "Bearer",
                  "scope": "user-read-currently-playing",
                  "expires_in": 3600,
                  "refresh_token": "refresh-token"
                }
                """, MediaType.APPLICATION_JSON ) );

        SpotifyTokenResponse tokens = service.exchangeCodeForTokens( "test-code" );

        assertThat( tokens.accessToken( ) ).isEqualTo( "access-token" );
        assertThat( tokens.tokenType( ) ).isEqualTo( "Bearer" );
        assertThat( tokens.scope( ) ).isEqualTo( "user-read-currently-playing" );
        assertThat( tokens.expiresIn( ) ).isEqualTo( 3600 );
        assertThat( tokens.refreshToken( ) ).isEqualTo( "refresh-token" );
    }
}
