# SDK Java da Assinafy

*Português · [Read in English](README.en.md)*

Cliente Java thread-safe para as 147 operações da [API Assinafy](https://api.assinafy.com.br/v1/docs),
incluindo documentos, assinaturas, templates, contratos, webhooks, OAuth 2.1 e os fluxos públicos do
signatário. O [contrato completo](docs/API_REFERENCE.md) documenta cada rota, payload, resposta e status.

## Requisitos e instalação

- JDK 25 LTS (`>=25,<26`) e TLS 1.2 ou superior.
- Maven Wrapper 3.10.0 para compilar o projeto.
- OkHttp e Jackson como dependências de runtime.

```xml
<dependency>
  <groupId>com.assinafy</groupId>
  <artifactId>assinafy-sdk</artifactId>
  <version>2.4.0</version>
</dependency>
```

Gradle: `implementation("com.assinafy:assinafy-sdk:2.4.0")`.

## Fluxo completo de assinatura

Use uma chave de API para agir no próprio workspace. Guarde credenciais em variáveis de ambiente e
compartilhe uma única instância de `AssinafyClient` na aplicação.

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.UploadAndRequestSignaturesResult;
import com.assinafy.sdk.request.UploadAndRequestSignaturesRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

AssinafyClient client = AssinafyClient.create(
    System.getenv("ASSINAFY_API_KEY"),
    System.getenv("ASSINAFY_ACCOUNT_ID")
);

UploadAndRequestSignaturesResult result = client.uploadAndRequestSignatures(
    UploadAndRequestSignaturesRequest.builder()
        .fileData(Files.readAllBytes(Path.of("contrato.pdf")))
        .fileName("contrato.pdf")
        .signers(List.of(
            UploadAndRequestSignaturesRequest.SignerEntry.builder()
                .name("Pessoa Exemplo")
                .email("signer@example.invalid")
                .build()
        ))
        .message("Por favor, assine o documento")
        .expiresAt(OffsetDateTime.now().plusDays(7).toString())
        .build()
);

String documentId = result.getDocument().getId();
String assignmentId = result.getAssignment().getId();

// Depois do webhook de certificação:
Document finalDocument = client.documents().details(documentId);
if ("certificated".equals(finalDocument.getStatus())) {
    Files.write(Path.of("contrato-assinado.pdf"), client.documents().download(documentId));
    Files.write(Path.of("certificado.pdf"),
        client.documents().download(documentId, "certificate-page"));
}
```

O helper valida antes do upload, envia o PDF, espera os metadados, reutiliza ou cria signatários e
cria uma atribuição `virtual`. Em falha posterior, tenta remover apenas recursos cuja criação foi
confirmada. Para campos posicionados, crie uma atribuição `collect` diretamente. Sempre:

1. Persista IDs e estime o custo com `assignments().estimateCostTyped(...)`.
2. Trate o processamento inicial (`metadata_ready`) e a certificação final (`certificated`) como fases distintas.
3. Consuma webhooks de forma idempotente e espere `certificated` antes de baixar artefatos finais.
4. Guarde `original`, `certificated`, `certificate-page`, `pades` ou `bundle` antes de excluir o documento.

Prazos aceitam data ou data/hora ISO 8601 pelo menos uma hora à frente. Uma data termina às 23:59
UTC; data/hora sem offset usa UTC; offset explícito é preservado.

## Verificação e notificações

| Verificação | Requisito | Notificação |
|---|---|---|
| `Email` | E-mail válido | zero ou `Email` |
| `Whatsapp` | Número E.164, por exemplo `+15555550123` | zero ou `Whatsapp` |
| `DigitalCertificate` | Recurso habilitado, CPF/CNPJ e atribuição `virtual` | zero, `Email` ou `Whatsapp` |
| `IdDocumentFaceMatchBasic` | Recurso habilitado, CPF e consentimento biométrico explícito | zero, `Email` ou `Whatsapp` |

Cada signatário aceita no máximo um canal. `step` define ordem contígua a partir de 1; signatários
no mesmo passo assinam em paralelo. A estimativa aceita uma lista de notificações vazia.

Biometria exige consentimento antes de `startIdentityVerification(accessCode, true)`. Não registre,
armazene no navegador nem altere o fragmento da URL de captura. Evidências existem somente durante
a revisão e não devem ser armazenadas em cache.

Assinatura ICP-Brasil A1/A3 usa `startCertificateSignature` e `completeCertificateSignature` com Web
PKI. Um certificado por passo; o endpoint comum de assinatura não conclui esse método. Confirme o
status `certificated` antes do download. Os fluxos completos e payloads estão na
[referência](docs/API_REFERENCE.md#deployed-extensions).

## Envelopes e assinatura incorporada

`envelopes().create(...)` envia até 20 documentos processados aos mesmos signatários; cada documento
mantém assinatura, status e artefatos próprios. Estime com `envelopes().estimateCost(...)`, use
`maxTotalCredits` para limitar a cobrança e persista uma chave de idempotência para a criação.
A mesma chave também é aceita em `documents().createFromTemplate(..., accountId, idempotencyKey)`.

Para iframe, registre a origem com `embed().addOrigin(...)`, selecione `embedded(true)` no signatário
Email/Whatsapp e crie uma sessão com `embed().createSession(documentId, assignmentId, signerId)`.
Entregue a URL somente ao frontend autorizado; ela expira e só pode ser trocada uma vez pela página
Assinafy. Não registre a URL ou o código de acesso. Biometria e certificados usam o fluxo hospedado.
As [operações completas](docs/API_REFERENCE.md#api-operations) incluem reenvio, cancelamento,
download ZIP, preferências push, política de notificações e saldo do workspace.

## OAuth 2.1 para aplicativos de marketplace

Use Authorization Code + PKCE com redirect HTTPS. `oauth().createAuthorizationUrl(...)` devolve
URL, `state`, verificador PKCE, emissor e `nonce`; mantenha o objeto inteiro no servidor. No callback,
`readAuthorizationCallback(...)` valida parâmetros e devolve o código para `exchangeCode(...)`.

Tokens de acesso duram uma hora. Cada refresh gira o refresh token e inicia novos 30 dias: serialize
refreshes por conexão e salve o novo conjunto atomicamente. Nunca reenvie um refresh cujo resultado
é incerto; peça reconexão. Revogue o token mais recente e nunca registre tokens, PKCE, `state`,
`nonce`, URLs de consentimento ou URLs de assinatura. `offline_access` não volta em `scope`.

## Webhooks

Crie endpoints com URL HTTPS pública. Há um por conta e até três em planos pagos. Ative
`signing_enabled` e salve imediatamente o `WebhookEndpoint.getSecret()` retornado na criação ou ao
habilitar assinatura. OAuth não lê o segredo, mas pode girá-lo com `webhooks:write`; a rotação invalida
o anterior imediatamente por padrão. `rotateEndpointSecret(id, true)` mantém o anterior por
24 horas; armazene o novo segredo e respeite `previous_secret_expires_at`.

```java
byte[] rawBody = requestBodyBytes;
if (!com.assinafy.sdk.util.WebhookSignature.verify(secret, requestHeaders, rawBody)) {
    throw new SecurityException("Assinatura de webhook inválida");
}
```

Verifique o corpo bruto antes de desserializar, deduplique por `webhook-id`, confirme `account_id` e
processe assincronamente. Responda 2xx rápido: a API faz até seis tentativas, com cerca de dez minutos
entre elas, e pausa o endpoint após dez eventos consecutivos sem entrega.

## Superfícies e erros

O cliente expõe `documents()`, `signers()`, `assignments()`, `contracts()`, `templates()`, `fields()`,
`envelopes()`, `embed()`, `tags()`, `webhooks()`, `workspaces()`, `users()`, `apiKeys()`, `authentication()`, `oauth()` e
`publicDocuments()`. Operações de conta usam o `accountId` padrão e oferecem overload explícito.

Sucessos retornam o `data` do envelope; OAuth retorna JSON plano e downloads retornam `byte[]`.
Entradas inválidas lançam `ValidationException` antes da rede. Respostas remotas usam
`AuthenticationException`, `RateLimitException`, `ApiException`, `OAuthException` ou
`NetworkException`. Consulte a [referência por operação](docs/API_REFERENCE.md) para todos os campos
de requisição/resposta e códigos documentados.

## Desenvolvimento

```bash
export JAVA_HOME=/usr/local/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
./mvnw clean verify
```

Testes live são opt-in: `./mvnw -Plive-api verify` com as variáveis `ASSINAFY_*` de sandbox.
Licença: [MIT](LICENSE).
