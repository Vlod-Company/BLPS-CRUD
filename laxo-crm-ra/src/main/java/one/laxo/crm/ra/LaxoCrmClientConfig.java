package one.laxo.crm.ra;

public record LaxoCrmClientConfig(
        String baseUrl,
        String sid,
        String crmName,
        Integer connectTimeoutMillis,
        Integer readTimeoutMillis,
        Integer funnelId,
        Integer orderStatusId,
        Integer orderUserMentor
) {
}
