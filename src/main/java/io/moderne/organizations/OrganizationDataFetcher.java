package io.moderne.organizations;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import io.moderne.organizations.types.CommitOption;
import io.moderne.organizations.types.Organization;
import io.moderne.organizations.types.RepositoryInput;
import io.moderne.organizations.types.User;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

@DgsComponent
public class OrganizationDataFetcher {
    List<OrganizationRepositories> ownership;

    public OrganizationDataFetcher(ObjectMapper mapper) throws IOException {
        this.ownership = mapper.readValue(
                getClass().getResourceAsStream("/ownership.json"),
                new TypeReference<>() {
                }
        );
    }

    @DgsQuery
    Flux<Organization> organizations(@InputArgument RepositoryInput repository) {
        return Flux.fromIterable(ownership)
                .filter(org -> org.matches(repository))
                .map(OrganizationDataFetcher::mapOrganization)
//                .concatWith(Flux.just(createForOrganizationName(repository))) // if you want a group per organization
//                .concatWith(Flux.just(Organization.newBuilder().id("ALL").name("ALL").allCommitOptions().build())) // if you want an "ALL" group
                ;
    }

    @DgsQuery
    Flux<Organization> userOrganizations(@InputArgument User user, @InputArgument OffsetDateTime at) {
        // everybody belongs to every organization, and the "default" organization is listed
        // first in the json that this list is based on, so it will be selected by default in the UI
        return Flux.fromIterable(ownership)
                .map(OrganizationDataFetcher::mapOrganization);
    }

    private static Organization mapOrganization(OrganizationRepositories org) {
        return Organization.newBuilder()
                .id(org.name())
                .name(org.name())
                .commitOptions(List.of(CommitOption.values()))
                .build();
    }
}
