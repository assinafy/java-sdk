# SDK Java da Assinafy

*Português · [Read in English](README.en.md)*

Cliente Java para a [API Assinafy](https://api.assinafy.com.br/v1/docs) — plataforma brasileira de
assinatura eletrônica de documentos. Cobre as 93 operações documentadas — upload e certificação de
documentos, gestão de signatários, solicitações de assinatura, templates, definições de campo, tags,
workspaces, webhooks, OAuth 2.1 e os fluxos self-service do signatário — atrás de modelos tipados,
exceções tipadas e um único cliente thread-safe.

Este documento é o guia completo em português: leia de cima para baixo e você terá percorrido o
caminho inteiro, do PDF em disco ao arquivo assinado. O manual de referência operação por operação
está em **[README.en.md](README.en.md)**, e o contrato de requisição/resposta de cada rota em
**[docs/API_REFERENCE.md](docs/API_REFERENCE.md)**.

## Sumário

1. [Requisitos](#requisitos)
2. [Instalação](#instalação)
3. [Autenticação](#autenticação)
4. [Configuração](#configuração)
5. [Como o SDK é organizado](#como-o-sdk-é-organizado)
6. [O ciclo de vida da assinatura](#o-ciclo-de-vida-da-assinatura)
7. [Métodos de verificação e notificação](#métodos-de-verificação-e-notificação)
8. [Certificado digital ICP-Brasil (A1 e A3)](#certificado-digital-icp-brasil-a1-e-a3)
9. [O atalho de uma chamada só](#o-atalho-de-uma-chamada-só)
10. [OAuth 2.1 — agir no workspace de outra pessoa](#oauth-21--agir-no-workspace-de-outra-pessoa)
11. [Templates, campos e tags](#templates-campos-e-tags)
12. [Self-service do signatário](#self-service-do-signatário)
13. [Webhooks](#webhooks)
14. [Workspaces, usuários e chaves de API](#workspaces-usuários-e-chaves-de-api)
15. [Tratamento de erros](#tratamento-de-erros)
16. [Paginação](#paginação)
17. [Ambientes](#ambientes)
18. [Desenvolvimento](#desenvolvimento)
19. [Documentação](#documentação)
20. [Licença](#licença)

## Requisitos

- JDK 25 (LTS). O build exige Java `>=25,<26`.
- Maven Wrapper fixado no Maven 3.9.16 — não é necessário ter Maven instalado no sistema.
- TLS 1.2 ou superior: o cliente HTTP padrão recusa TLS 1.0 e 1.1.

As dependências de runtime são OkHttp e Jackson. O jar publicado declara
`Automatic-Module-Name: com.assinafy.sdk`.

## Instalação

As tags de release publicam no GitHub Packages, e o GitHub exige autenticação mesmo para pacotes
Maven públicos. Exporte um usuário do GitHub e um personal access token clássico com `read:packages`:

```bash
export GITHUB_ACTOR=seu-usuario-github
export GITHUB_TOKEN=seu-personal-access-token-classico
```

Referencie essas variáveis no `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>${env.GITHUB_ACTOR}</username>
            <password>${env.GITHUB_TOKEN}</password>
        </server>
    </servers>
</settings>
```

Depois adicione o repositório e a dependência ao seu projeto:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/assinafy/java-sdk</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.assinafy</groupId>
    <artifactId>assinafy-sdk</artifactId>
    <version>1.11.0</version>
</dependency>
```

Trabalhando a partir de um checkout do código? Instale o artefato no seu repositório local primeiro:

```bash
./mvnw install
```

## Autenticação

A API aceita três credenciais, e a escolha depende de **em qual workspace o seu código age**.

| Credencial | Age em | Use quando |
| --- | --- | --- |
| Chave de API (`X-Api-Key`) | O **seu próprio** workspace | Você automatiza a sua própria conta. É a opção recomendada para integrações de servidor. |
| Token de acesso (`Authorization: Bearer`) | O workspace da sessão | Você já tem um token de uma sessão interativa, ou um token OAuth. |
| Token OAuth 2.1 | O workspace de **outra pessoa**, com a permissão dela | Você constrói um produto que outros clientes Assinafy conectam. Veja [OAuth 2.1](#oauth-21--agir-no-workspace-de-outra-pessoa). |

```java
// Preferido: header X-Api-Key
AssinafyClient apiKeyClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("sua-chave-de-api")
        .accountId("seu-account-id")
        .build()
);

// Authorization: Bearer — token de acesso, vindo de authentication().login(...) ou do OAuth
AssinafyClient bearerClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .token("jwt-token")
        .accountId("seu-account-id")
        .build()
);
```

Quando as duas estão configuradas, a chave de API vence. As operações voltadas ao signatário usam uma
quarta credencial — o código de acesso do signatário — passada por chamada, e não configurada no
cliente. Operações públicas não exigem credencial nenhuma; construa um cliente sem credenciais para
elas.

## Configuração

| Opção | Tipo | Padrão | Descrição |
|-------|------|--------|-----------|
| `apiKey` | String | — | Credencial preferida, enviada como `X-Api-Key`. |
| `token` | String | — | Token de acesso bearer, usado quando não há chave de API. |
| `accountId` | String | — | Workspace padrão para operações com escopo de conta. |
| `baseUrl` | String | `https://api.assinafy.com.br/v1` | URL base HTTPS da API. HTTP puro é rejeitado, exceto em testes de loopback; use `AssinafyClientOptions.SANDBOX_BASE_URL` para o sandbox. |
| `timeoutMs` | long | `30000` | Timeout de chamada, conexão, leitura e escrita, em milissegundos. |
| `logger` | Logger | No-op | Callback de diagnóstico estruturado. Um logger que lança exceção nunca afeta a chamada à API. |

Dois métodos de fábrica cobrem os casos comuns:

```java
AssinafyClient client = AssinafyClient.create("chave-de-api", "account-id");

AssinafyClientOptions extras = AssinafyClientOptions.builder()
    .baseUrl(AssinafyClientOptions.SANDBOX_BASE_URL)
    .timeoutMs(60_000)
    .build();
AssinafyClient sandboxClient = AssinafyClient.create("chave-de-api", "account-id", extras);
```

## Como o SDK é organizado

**Um cliente, vários recursos.** `AssinafyClient` é dono do transporte HTTP e expõe um acessor por
área da API: `documents()`, `signers()`, `assignments()`, `templates()`, `fields()`, `tags()`,
`workspaces()`, `webhooks()`, `users()`, `apiKeys()`, `authentication()`, `oauth()` e
`publicDocuments()`. Os acessores devolvem as instâncias que o cliente possui, então guardar a
referência de um recurso equivale a guardar o cliente.

**O cliente é thread-safe** com o transporte OkHttp padrão e foi feito para ser criado uma vez e
compartilhado. O OkHttp libera conexões e threads ociosas sozinho; não há shutdown a chamar.

**Um tipo Java por recurso da API.** A API devolve o mesmo schema `Document` no upload, na listagem,
na busca, no get, no rename e na criação a partir de template — e o SDK espelha isso: todos esses
métodos devolvem `Document`. Um campo que determinada resposta não preenche vem `null`: um documento
recém-enviado não tem `assignment` nem `pages` até o processamento chegar em `metadata_ready`.
`Workspace`, `Template`, `Signer` e `Assignment` funcionam do mesmo jeito.

**Os envelopes são desembrulhados para você.** Os corpos JSON de sucesso são
`{ "status": inteiro, "message": string, "data": ... }`, e os métodos do SDK devolvem o `data`. Um
método `void` descarta o envelope de sucesso e também aceita um corpo 2xx vazio. Métodos binários
devolvem `byte[]` cru, sem decodificação JSON. Um HTTP não-2xx — ou um `status` numérico não-2xx
dentro de um envelope 200 — vira `ApiException`.

As rotas OAuth são a exceção deliberada: por exigência do RFC 6749 e do OpenID Connect, elas
respondem com objetos JSON planos (`access_token` na raiz, ou `{error, error_description}`) em vez do
envelope. O SDK trata as duas formas.

**Escopo de conta.** Operações com escopo de conta usam o `accountId` configurado no cliente. Todas
elas também têm uma sobrecarga que recebe o ID explicitamente, então um único cliente atende vários
workspaces. `workspaces()` sempre recebe o ID explícito, porque suas operações são *sobre* o
workspace, e não *dentro* dele.

## O ciclo de vida da assinatura

O caminho mais curto, do PDF em disco à solicitação de assinatura despachada:

```java
import com.assinafy.sdk.AssinafyClient;
import com.assinafy.sdk.AssinafyClientOptions;
import com.assinafy.sdk.models.Assignment;
import com.assinafy.sdk.models.Document;
import com.assinafy.sdk.models.Signer;
import com.assinafy.sdk.request.CreateAssignmentRequest;
import com.assinafy.sdk.request.CreateSignerRequest;
import com.assinafy.sdk.request.SignerReference;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

AssinafyClient client = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey(System.getenv("ASSINAFY_API_KEY"))
        .accountId(System.getenv("ASSINAFY_ACCOUNT_ID"))
        .build()
);

byte[] fileData = Files.readAllBytes(Path.of("contrato.pdf"));
Document document = client.documents().upload(fileData, "contrato.pdf");
client.documents().waitUntilReady(document.getId());

Signer signer = client.signers().findOrCreate(
    CreateSignerRequest.builder()
        .fullName("Maria Silva")
        .email("maria@example.invalid")
        .build()
);

Assignment assignment = client.assignments().create(
    document.getId(),
    CreateAssignmentRequest.builder()
        .method("virtual")
        .signers(List.of(SignerReference.ofId(signer.getId())))
        .message("Por favor, assine este documento")
        .build()
);
```

Cada passo, em detalhe:

### 1. Enviar o documento

`documents().upload(bytes, nomeDoArquivo)` envia um PDF como `multipart/form-data` e devolve o
`Document` criado. O limite é 25 MB, verificado no cliente antes do envio. Há uma sobrecarga que
aceita metadados e um account ID explícito.

O documento nasce com status `uploaded` e ainda não tem páginas nem assignment.

### 2. Esperar o processamento

A plataforma extrai páginas e metadados de forma assíncrona. `documents().waitUntilReady(id)` faz
polling até o status chegar a `metadata_ready`, `pending_signature` ou `certificated`, e lança se o
documento terminar em `failed`, `rejected_by_signer`, `rejected_by_user` ou `expired`. A sobrecarga
`waitUntilReady(id, maxWaitMs, pollIntervalMs)` controla o tempo total e o intervalo.

Criar um assignment antes do documento ficar pronto é rejeitado pelo servidor — não pule este passo.

### 3. Resolver os signatários

Signatários são recursos persistentes do workspace, reutilizados entre documentos.
`signers().findOrCreate(...)` devolve o signatário cujo e-mail bate (comparação sem diferenciar
maiúsculas) ou cria um novo. `signers().create(...)` sempre cria.

Um signatário precisa de `fullName` e de pelo menos um canal de entrega: `email` ou
`whatsappPhoneNumber`. O CPF/CNPJ vai em `cpf` e é gravado pelo SDK através do update documentado
(`government_id`), com os não-dígitos removidos.

```java
Signer porWhatsapp = client.signers().findOrCreate(
    CreateSignerRequest.builder()
        .fullName("João Souza")
        .whatsappPhoneNumber("+5511999999999")
        .cpf("123.456.789-09")
        .build()
);
```

### 4. Estimar o custo e solicitar as assinaturas

`assignments().estimateCostTyped(documentId, request)` devolve a previsão de créditos **sem** criar
nada, incluindo `hasSufficientResources`. Use antes de gastar créditos de notificação.

`assignments().create(documentId, request)` cria o assignment e dispara as notificações. O `method`
é `virtual` (assinatura à distância, o padrão) ou `collect` (coleta de campos preenchidos, que exige
`entries` não vazio).

```java
CreateAssignmentRequest pedido = CreateAssignmentRequest.builder()
    .method("virtual")
    .signers(List.of(
        SignerReference.builder().id(maria.getId()).step(1).build(),
        SignerReference.builder().id(joao.getId()).step(2)
            .verificationMethod("Whatsapp")
            .notificationMethods(List.of("Whatsapp"))
            .build()
    ))
    .message("Contrato de prestação de serviços")
    .expiresAt("2026-12-31T23:59:59Z")
    .build();

CostEstimate previsao = client.assignments().estimateCostTyped(document.getId(), pedido);
if (Boolean.TRUE.equals(previsao.getHasSufficientResources())) {
    Assignment assignment = client.assignments().create(document.getId(), pedido);
}
```

**Ordem de assinatura.** Defina `step` para assinar em etapas. Ou todos os signatários informam
`step`, ou nenhum informa, e os passos precisam ser contíguos a partir de 1 — o SDK valida isso antes
de enviar. O signatário do passo 1 é notificado na criação; os passos seguintes só são notificados
quando o passo anterior termina.

### 5. Acompanhar o andamento

```java
SigningProgress progresso = client.documents().getSigningProgress(document.getId());
boolean pronto = client.documents().isFullySigned(document.getId());
List<DocumentActivity> trilha = client.documents().activities(document.getId());
```

A trilha de atividades devolve todos os eventos registrados, cada um com um snapshot do `payload` do
evento e a `origin` da requisição (`ip`, `user-agent`).

Precisa reenviar a notificação de um signatário? `assignments().estimateResendCostTyped(...)` prevê o
custo e `assignments().resendNotification(...)` reenvia. Para prorrogar o prazo,
`assignments().resetExpiration(documentId, assignmentId, novaData)`.

### 6. Baixar os artefatos

| Artefato | Conteúdo |
| --- | --- |
| `original` | O PDF enviado, como recebido |
| `certificated` | O documento assinado, com a certificação da plataforma |
| `certificate-page` | Apenas a página de certificação |
| `pades` | Assinaturas ICP-Brasil dos signatários + caixa de certificação — só existe em documentos que tiveram signatários por certificado digital |
| `bundle` | Zip com `original`, `certificated` e `certificate-page`, mais o `pades` quando houver |

```java
byte[] assinado = client.documents().download(document.getId());                  // certificated
byte[] pades    = client.documents().download(document.getId(), "pades");
byte[] miniatura = client.documents().thumbnail(document.getId());
```

Um download não-2xx lança em vez de devolver o corpo de erro como se fossem os bytes do arquivo.

A verificação pública confere um documento assinado pelo hash da assinatura, sem autenticação:
`documents().verifyTyped(hash)`.

### 7. Apagar quando terminar

`documents().delete(id)` remove o documento. O `uploadAndRequestSignatures` usa a mesma rota para
desfazer o próprio upload quando um passo posterior falha.

## Métodos de verificação e notificação

Definidos por signatário ao criar o assignment. O método de **verificação** (como o signatário prova
quem é antes de assinar) e o de **notificação** (como ele é avisado) são **acoplados**: envie um, os
dois ou nenhum — o lado que faltar é inferido. Sem nenhum dos dois, ambos assumem `Email`.

| Método de verificação | Como funciona | Requisitos | Custo por signatário |
| --- | --- | --- | --- |
| `Email` *(padrão)* | Código de uso único (OTP) por e-mail, exigido antes de assinar | Signatário com e-mail | 0 créditos |
| `Whatsapp` | Código de uso único (OTP) por WhatsApp | `whatsappPhoneNumber` no signatário; só em planos pagos | 0,45 crédito (a notificação WhatsApp, que este método exige) |
| `DigitalCertificate` | O signatário assina com o **próprio certificado ICP-Brasil (A1 ou A3)**, pela extensão de navegador Web PKI, gerando uma assinatura **PAdES qualificada** | Recurso Certificado Digital na conta; CPF em `governmentId`; um signatário por certificado em cada passo | 2 créditos + o custo da notificação |

| Método de notificação | Entrega | Requisitos | Custo por signatário |
| --- | --- | --- | --- |
| `Email` | E-mail com o link para assinar | Signatário com e-mail | 0 créditos |
| `Whatsapp` | Mensagem de WhatsApp com o link para assinar | `whatsappPhoneNumber`; só em planos pagos | 0,45 crédito |

Combinações permitidas — qualquer outra devolve `400`:

| Verificação | Notificações aceitas |
| --- | --- |
| `Email` | `Email` |
| `Whatsapp` | `Whatsapp` |
| `DigitalCertificate` | `Email` **ou** `Whatsapp` |

Apenas um método de notificação por signatário. Nenhum método de verificação tem preço próprio: o que
se cobra é a **notificação** com que ele anda junto — mais, no caso do certificado digital, a própria
assinatura. Use `assignments().estimateCostTyped(...)` para ver o total exato antes de criar.

## Certificado digital ICP-Brasil (A1 e A3)

Exige o recurso **Certificado Digital** na conta (planos Standard e Pro), CPF ou CNPJ em
`governmentId` do signatário, e exatamente **um signatário por certificado naquele passo**. Um CPF
exige o certificado daquela pessoa (e-CPF, ou e-CNPJ que a nomeie como representante legal); um CNPJ
exige um e-CNPJ da empresa. O SDK valida a regra de "um por passo" antes de enviar.

Antes de abrir o assignment, o signatário precisa confirmar os dados de identidade
(`signers().confirmSignerData(...)`) e aceitar os termos (`signers().acceptTerms(...)`). O endpoint
comum de assinatura **rejeita** signatários por certificado com `400` — a assinatura deles é
produzida por um handshake de dois passos com a extensão Web PKI:

```java
// 1. Abre a operação e devolve o token que o navegador precisa assinar
String token = client.signers().startCertificateSignature(signerAccessCode);

// 2. O navegador assina o token com o certificado A1/A3 do signatário, pela extensão Web PKI,
//    e devolve o valor assinado ao seu backend

// 3. Conclui a assinatura
String nomeNoCertificado =
    client.signers().completeCertificateSignature(signerAccessCode, tokenAssinado);
```

> Essas duas rotas são extensões implantadas **somente em produção**: o sandbox não as expõe e elas
> não constam do documento OpenAPI publicado.

Concluído o fluxo, baixar o artefato `pades` devolve a assinatura PAdES qualificada.

## O atalho de uma chamada só

`uploadAndRequestSignatures` faz o caminho inteiro numa chamada: envia o PDF, espera o processamento,
resolve os signatários (reaproveitando por e-mail), grava o CPF e cria o assignment `virtual` que
dispara as notificações. Entradas só com WhatsApp usam WhatsApp para verificação e notificação
automaticamente.

```java
UploadAndRequestSignaturesResult resultado = client.uploadAndRequestSignatures(
    UploadAndRequestSignaturesRequest.builder()
        .fileData(fileData)
        .fileName("contrato.pdf")
        .signers(List.of(
            UploadAndRequestSignaturesRequest.SignerEntry.builder()
                .name("Maria Silva")
                .email("maria@example.invalid")
                .cpf("123.456.789-09")
                .build()
        ))
        .message("Por favor, assine este documento")
        .build()
);
```

O método bloqueia por padrão e tem efeitos colaterais visíveis de fora — cria signatários e dispara
notificações. Se um passo depois do upload falhar, ele apaga o documento enviado e os signatários que
ele mesmo criou; falhas na limpeza são anexadas à exceção original como *suppressed*. Quando o
resultado da criação do assignment fica indeterminado (erro 5xx ou de rede), o SDK reconcilia pelos
detalhes do documento antes de desfazer, e em último caso **preserva** os recursos em vez de apagar
uma solicitação que pode ter sido despachada.

## OAuth 2.1 — agir no workspace de outra pessoa

Use OAuth quando o seu produto é **conectado pelos seus usuários** e age no workspace deles, sem você
nunca tocar na senha ou na chave de API dessas pessoas. Automatizando a sua própria conta? Continue
com a chave de API; nada desta seção se aplica.

|  | Chave de API | OAuth |
| --- | --- | --- |
| Age em | O **seu** workspace | O workspace de **outra pessoa**, com a permissão dela |
| Pode fazer | Tudo que a sua conta pode | Só o que o usuário aprovou |
| O usuário pode desligar | Não | Sim, a qualquer momento |

Dois hosts, de propósito: a tela de consentimento vive no servidor de autorização
(`https://auth.assinafy.com.br`) e os endpoints de token, revogação e userinfo vivem nesta API. O SDK
lê as URLs dos documentos de metadados publicados, em vez de fixá-las no código.

### Registrar a aplicação

No app da Assinafy, em **Configurações → Aplicações OAuth → Nova aplicação**. Você precisa ser
**owner** do workspace e o plano precisa incluir aplicações OAuth.

| Campo | O que colocar |
| --- | --- |
| Nome | O que o usuário vê na tela de aprovação |
| Descrição | Uma frase sobre o que a aplicação faz com os documentos dele |
| URIs de redirecionamento | Onde o usuário volta depois de aprovar. Precisa ser `https://`, sem `#`, e é comparada **caractere a caractere**: `…/callback` e `…/callback/` são diferentes. Cadastre uma por ambiente |
| Permissões | O **máximo** que a aplicação vai pedir algum dia. Dá para pedir menos na hora de conectar, nunca mais |
| Tipo | **Confidencial** se o código roda num servidor seu; **Público** se roda no dispositivo do usuário e não consegue guardar segredo. Não muda depois |

O `client_secret` é mostrado **uma única vez**. Aplicações públicas não recebem segredo nenhum e se
autenticam só por PKCE.

### Permissões (escopos)

O enum `OAuthScope` cobre a lista publicada:

| Escopo | Permite |
| --- | --- |
| `DOCUMENTS_READ` | Ler documentos, páginas, tags, signatários, assignments e atividades |
| `DOCUMENTS_WRITE` | Criar, alterar e apagar documentos e enviá-los para assinatura |
| `TEMPLATES_READ` | Ler templates, suas páginas, papéis, campos e tags |
| `TEMPLATES_WRITE` | Criar, alterar e apagar templates |
| `ACCOUNT_READ` | Ler perfil, tema e logo do workspace |
| `WEBHOOKS_WRITE` | Configurar e desativar a assinatura de webhooks do workspace |
| `OPENID` | Receber um `id_token` identificando o usuário e habilitar o userinfo |
| `PROFILE` | Ler o nome do usuário |
| `EMAIL` | Ler o e-mail do usuário e se ele é verificado |
| `OFFLINE_ACCESS` | Receber um refresh token, para continuar funcionando com o usuário ausente |

Peça o mínimo: o usuário aprova tudo o que você pediu, ou nada. `DOCUMENTS_WRITE` gasta créditos de
notificação do workspace, porque enviar para assinatura notifica signatários. Cobrança, assinatura do
plano, quadro de membros, credenciais e administração **nunca** ficam acessíveis a um token OAuth,
qualquer que seja o escopo.

### Conectar um usuário

```java
import com.assinafy.sdk.models.OAuthAuthorizationRequest;
import com.assinafy.sdk.models.OAuthTokens;
import com.assinafy.sdk.models.enums.OAuthScope;
import com.assinafy.sdk.request.AuthorizationUrlRequest;
import com.assinafy.sdk.request.OAuthClient;

// Um cliente sem credenciais basta para todo o fluxo OAuth.
AssinafyClient client = new AssinafyClient(new AssinafyClientOptions());

OAuthClient app = OAuthClient.confidential(
    System.getenv("ASSINAFY_CLIENT_ID"),
    System.getenv("ASSINAFY_CLIENT_SECRET")
);
// Aplicação pública: OAuthClient.publicClient(System.getenv("ASSINAFY_CLIENT_ID"))

// 1. Antes de redirecionar o navegador
OAuthAuthorizationRequest pedido = client.oauth().createAuthorizationUrl(
    AuthorizationUrlRequest.builder()
        .clientId(app.clientId())
        .redirectUri("https://meuapp.com/oauth/callback")
        .scopes(OAuthScope.DOCUMENTS_READ, OAuthScope.DOCUMENTS_WRITE, OAuthScope.OFFLINE_ACCESS)
        .build()
);
session.setAttribute("assinafy.oauth", pedido);   // guarda state + codeVerifier + issuer
response.sendRedirect(pedido.url());              // navegação de página inteira, não AJAX
```

O SDK gera o par PKCE (`S256`, obrigatório inclusive para aplicações confidenciais), o `state` e —
quando você pede `OPENID` — o `nonce`. Gere um novo a cada tentativa de conexão: reaproveitar um
verifier ou um `state` anula o PKCE e a proteção contra CSRF.

```java
// 2 e 3. No seu redirect URI
OAuthAuthorizationRequest guardado =
    (OAuthAuthorizationRequest) session.getAttribute("assinafy.oauth");

String code = client.oauth().readAuthorizationCallback(request.getQueryString(), guardado);

// 4. Troca o código por tokens — no servidor. O código vale 60 segundos e é de uso único.
OAuthTokens tokens = client.oauth().exchangeCode(
    app, code, guardado.codeVerifier(), "https://meuapp.com/oauth/callback");
```

`readAuthorizationCallback` confere, antes de qualquer outra coisa, que o `state` é o seu (comparação
em tempo constante) e que o `iss` é o servidor esperado; só então olha se o servidor reportou erro. Um
consentimento recusado chega como `?error=access_denied` e vira `OAuthException`, não uma falha de
HTTP. Um `iss` ausente é tratado como um `iss` errado, porque o servidor sempre o envia (RFC 9207).

```java
// 5. O token vale para exatamente um workspace — descubra qual
AssinafyClient conectado = new AssinafyClient(
    AssinafyClientOptions.builder().token(tokens.getAccessToken()).build());

String accountId = conectado.workspaces().list().getData().get(0).getId();
```

Guarde o `accountId` junto dos tokens e leia `tokens.getScope()` em vez de supor que tudo o que você
pediu foi concedido.

### Renovar, identificar e desconectar

```java
// Access token dura 1 hora. Com OFFLINE_ACCESS, renove sem o usuário:
OAuthTokens novos = client.oauth().refreshToken(app, conexao.getRefreshToken());
conexao.salvar(novos.getRefreshToken());   // ANTES de usar qualquer outra coisa da resposta

// Daqui em diante, chame a API com o access token renovado
conectado = new AssinafyClient(
    AssinafyClientOptions.builder().token(novos.getAccessToken()).build());

// Quem aprovou? (exige OPENID; nome exige PROFILE e e-mail exige EMAIL)
OAuthUserInfo quem = conectado.oauth().userInfo();

// Ao desconectar, revogue em vez de só apagar a sua cópia — e revogue o refresh token salvo por
// último, nunca uma cópia antiga: cada renovação aposentou o anterior
client.oauth().revokeToken(app, conexao.getRefreshToken(), "refresh_token");
```

> **Refresh tokens rodam.** Cada renovação devolve um novo e aposenta o anterior. Um refresh token
> reapresentado não pode ser distinguido de um roubado sendo replicado, então ele encerra a **conexão
> inteira** e o usuário precisa conectar de novo. Portanto: salve o novo refresh token antes de fazer
> qualquer outra coisa com a resposta, e nunca rode duas renovações ao mesmo tempo na mesma conexão.
> `refreshToken` só retorna quando a resposta traz um refresh token novo; caso contrário, lança
> `ValidationException` e o usuário precisa conectar de novo.
>
> **Nunca reenvie um refresh token depois de uma falha que pode ter chegado ao servidor** — timeout,
> conexão caída, `5xx`: a primeira tentativa pode já tê-lo aposentado. Releia o token salvo; se ainda
> for o que você enviou, peça ao usuário para conectar de novo. Só é seguro repetir uma falha que
> comprovadamente aconteceu antes do envio: uma `NetworkException` causada por
> `UnknownHostException` (DNS), `ConnectException` (conexão recusada) ou `SSLHandshakeException`.
> O próprio SDK nunca reenvia uma chamada ao endpoint de token: timeout ou conexão caída chega como
> `NetworkException`, e um `503` como `ApiException`.

Duas verdades por trás da maioria dos bugs de integração: **um token vale para um único workspace**
(qualquer outro devolve `403`, mesmo um do mesmo usuário — conecte cada workspace separadamente), e
**um refresh token vale 30 dias**: cada renovação devolve um novo, válido por mais 30 dias, então a
conexão só expira depois de 30 dias sem renovação — e aí o usuário precisa conectar de novo.

`tokens.getIdToken()` volta exatamente como chegou. Valide-o com uma biblioteca OpenID Connect antes
de confiar nele — chave RS256 do `jwks_uri` pelo `kid`, `iss`, `aud` igual ao seu `client_id`, `exp`
e `nonce` igual a `guardado.nonce()` — ou leia os dados do usuário em `userInfo()`.

### Descoberta

As URLs vêm dos metadados publicados, e o SDK as lê sozinho. Se você quiser inspecioná-las:

```java
OAuthProtectedResourceMetadata recurso = client.oauth().protectedResourceMetadata();
OAuthAuthorizationServerMetadata servidor = client.oauth().authorizationServerMetadata();
```

O SDK valida que o `issuer` do documento coincide com o host de onde ele foi buscado (RFC 8414 §3.3):
um documento que discorda não é autoritativo e é rejeitado. Para pular a descoberta — e a chamada de
rede que ela custa — informe `authorizationEndpoint` e `issuer` no
`AuthorizationUrlRequest`.

As chamadas aos endpoints de token e revogação **nunca** carregam o seu `X-Api-Key` nem o seu bearer:
elas autenticam a *aplicação* pelo `client_id`/`client_secret`, e mandar uma credencial de workspace
para uma rota que não tem uso para ela seria vazá-la.

### Erros de OAuth

`OAuthException` (subtipo de `ApiException`) expõe o código legível por máquina em `getError()` e a
explicação em `getErrorDescription()`.

| Código | Causa habitual |
| --- | --- |
| `access_denied` | O usuário recusou |
| `invalid_grant` | Código expirado ou já usado, `code_verifier` ou `redirect_uri` errado; refresh token já usado, ou o usuário reconectou com outras permissões |
| `invalid_client` | `client_id` ou segredo errado, ou aplicação desativada |
| `invalid_scope` | Escopo para o qual a aplicação não está registrada |
| `invalid_target` | `resource` diferente do que foi autorizado |
| `unsupported_grant_type` | Só existem `authorization_code` e `refresh_token` |

Nas chamadas comuns da API com um token OAuth: `401` significa token expirado, revogado ou não
enviado como `Bearer` — renove e, se não der, peça para reconectar. Um `403` com
`WWW-Authenticate: Bearer error="insufficient_scope"` nomeia a permissão que falta; trate como um
convite a reconectar pedindo aquele escopo, não como algo a repetir. Um `403` sem esse header tem
outra causa: outro workspace, o papel do usuário, ou uma área que tokens OAuth nunca alcançam.

### Antes de ir para produção

- Um verifier PKCE e um `state` novos a cada tentativa de conexão
- `state` e `iss` conferidos no redirect URI — o SDK faz isso em `readAuthorizationCallback`
- `client_secret` só no servidor, nunca em app mobile, código de navegador ou repositório
- O novo refresh token salvo antes de usar, e uma renovação por vez por conexão
- `401` tratado: renovar e, falhando, pedir para reconectar
- O ID do workspace guardado por conexão, e o `scope` devolvido realmente lido
- Toda URI de redirecionamento de produção cadastrada, `https://` e exata
- Só as permissões necessárias
- Tokens revogados quando o usuário desconecta

## Templates, campos e tags

**Templates** são documentos reutilizáveis com papéis e campos posicionados.
`documents().createFromTemplate(templateId, request)` gera um documento a partir de um template, e
`documents().estimateCostFromTemplateTyped(...)` prevê o custo antes. As mesmas regras de acoplamento
verificação/notificação e de ordem de assinatura valem aqui.

**Definições de campo** (`fields()`) são tipos de campo reutilizáveis do workspace — CPF, data, texto
livre. `fields().validate(fieldId, valor, signerAccessCode)` e
`fields().validateMultiple(entradas, signerAccessCode)` conferem valores contra a definição antes de
você enviá-los. `fields().listTypes()` lista os tipos disponíveis.

**Tags** (`tags()`) são rótulos do workspace. Documentos recebem tags **por nome**: a plataforma
vincula uma tag existente pelo nome e cria uma nova quando o nome não existe.

```java
client.documents().appendTags(document.getId(), List.of("contratos", "2026"));
client.documents().replaceTags(document.getId(), List.of("arquivado"));
client.documents().detachTag(document.getId(), tagId);   // desanexar usa o ID
```

`appendTagIds` e `replaceTagIds` existem para quem tem os IDs em mãos: o SDK resolve os nomes
correspondentes antes de enviar. Apagar uma tag ainda vinculada a um documento devolve `409` a menos
que você force.

## Self-service do signatário

As rotas voltadas ao signatário usam o **código de acesso do signatário**, passado a cada chamada e
não configurado no cliente. São elas que um portal de assinatura próprio consome:

```java
Signer eu = client.signers().getSelf(codigo);
client.signers().acceptTerms(codigo);
client.signers().confirmSignerData(documentId, codigo, Map.of("government_id", "12345678909"));
client.signers().verifyEmail(codigo, "123456");            // o OTP recebido
client.signers().uploadSignature(codigo, "signature", pngBytes);

Map<String, Object> paraAssinar = client.assignments().getForSigner(codigo);
client.assignments().sign(documentId, assignmentId, codigo, itens);
client.assignments().decline(documentId, assignmentId, codigo, "Valores divergentes");
```

Também há listagem, busca e download dos documentos do signatário, além de assinar ou recusar vários
de uma vez (`signMultiple`, `declineMultiple`).

## Webhooks

```java
client.webhooks().register(RegisterWebhookRequest.builder()
    .url("https://meuapp.com/webhooks/assinafy")
    .events(List.of("document_ready", "assignment_completed"))
    .build());

WebhookSubscription atual = client.webhooks().get();
List<WebhookEventTypeInfo> tipos = client.webhooks().listEventTypes();

PaginatedResult<WebhookDispatch> entregas = client.webhooks().listDispatches();
client.webhooks().retryDispatch(dispatchId);

client.webhooks().inactivate();   // para a entrega sem apagar a inscrição
```

Há uma inscrição por workspace: registrar de novo substitui a anterior.

## Workspaces, usuários e chaves de API

```java
PaginatedResult<Workspace> meus = client.workspaces().list();
Workspace w = client.workspaces().get(accountId);
AccountTheme tema = client.workspaces().getTheme(accountId);
byte[] logo = client.workspaces().downloadLogo(accountId);
List<DocumentStatsRow> kpis = client.workspaces().stats(accountId, "monthly", null);

AuthUser eu = client.users().get();
NotificationPreferences prefs = client.users().getNotificationPreferences();

ApiKey nova = client.apiKeys().create("sua-senha");   // devolve a chave inteira uma única vez
```

`workspaces().delete(accountId, true)` cancela uma assinatura paga ativa e apaga o workspace — sem o
`force`, o servidor responde `400` listando os bloqueios.

## Tratamento de erros

Todas as falhas do SDK são `RuntimeException`, sob uma raiz comum:

```
AssinafyException
├── ValidationException      entrada do chamador inválida, detectada antes de enviar
├── NetworkException         a requisição não chegou a completar
└── ApiException             a API respondeu com erro (statusCode, responseData, headers)
    ├── AuthenticationException   401 e 403
    ├── RateLimitException        429
    └── OAuthException            corpo de erro RFC 6749 (getError, getErrorDescription)
```

```java
try {
    client.assignments().create(documentId, pedido);
} catch (RateLimitException e) {
    // recuar e repetir
} catch (AuthenticationException e) {
    // credencial ausente, expirada ou sem permissão
} catch (ApiException e) {
    log.error("A API respondeu {}: {}", e.getStatusCode(), e.getMessage());
} catch (ValidationException e) {
    log.error("Requisição inválida: {}", e.getErrors());
}
```

## Paginação

As listagens devolvem `PaginatedResult<T>`, com `getData()` e `getMeta()` (página atual, itens por
página, total e número de páginas, lidos dos headers `X-Pagination-*`).

```java
ListParams params = ListParams.builder()
    .page(1)
    .perPage(50)          // máximo 100
    .search("contrato")
    .sort("-created_at")
    .build();

PaginatedResult<Document> pagina = client.documents().list(params);
```

`page` e `perPage` são validados no cliente antes do envio.

## Ambientes

| | |
| --- | --- |
| Produção | `https://api.assinafy.com.br/v1` |
| Sandbox | `AssinafyClientOptions.SANDBOX_BASE_URL` |

O sandbox é gratuito e espelha a produção para testar a integração de ponta a ponta — com a exceção
das rotas de certificado digital, que existem apenas em produção.

## Desenvolvimento

```bash
# Testes unitários, sem chamadas à API
./mvnw test

# Build completo: testes, Javadoc, jar, sources e javadoc jars
./mvnw verify

# Testes de integração contra a API real (exige credenciais no ambiente)
./mvnw -Plive-api verify
```

O build é estrito: `-Xlint:all -Werror` no compilador e `doclint:all` com `failOnWarnings` no
Javadoc, então um aviso quebra o `verify`.

## Documentação

- **[README.en.md](README.en.md)** — referência completa por operação, em inglês
- **[docs/API_REFERENCE.md](docs/API_REFERENCE.md)** — contrato de requisição/resposta rota por rota
- [Documentação da API](https://api.assinafy.com.br/v1/docs)

## Licença

Distribuído sob a licença [MIT](LICENSE).
