# SDK Java da Assinafy

*Português · [Read in English](README.en.md)*

Cliente Java para a [API Assinafy](https://api.assinafy.com.br/v1/docs) — plataforma brasileira de
assinatura eletrônica de documentos. Cobre as 89 operações documentadas — upload e certificação de
documentos, gestão de signatários, solicitações de assinatura, templates, definições de campo, tags,
workspaces, webhooks e os fluxos self-service do signatário — atrás de modelos tipados, exceções
tipadas e um único cliente thread-safe.

> **Referência completa em inglês.** Este documento cobre instalação, autenticação e os fluxos
> principais. O manual de referência por operação está em **[README.en.md](README.en.md)**.

## Requisitos

- JDK 25 (LTS). O build exige Java `>=25,<26`.
- Maven Wrapper fixado no Maven 3.9.16 — não é necessário ter Maven instalado no sistema.

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
    <version>1.7.0</version>
</dependency>
```

Trabalhando a partir de um checkout do código? Instale o artefato no seu repositório local primeiro:

```bash
./mvnw install
```

## Autenticação

A API aceita duas credenciais. Prefira a chave de API para integrações de servidor:

```java
// Preferido: header X-Api-Key
AssinafyClient apiKeyClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .apiKey("sua-chave-de-api")
        .accountId("seu-account-id")
        .build()
);

// Authorization: Bearer — token de acesso, vindo de authentication().login(...)
AssinafyClient bearerClient = new AssinafyClient(
    AssinafyClientOptions.builder()
        .token("jwt-token")
        .accountId("seu-account-id")
        .build()
);
```

Quando as duas estão configuradas, a chave de API vence. As operações voltadas ao signatário usam uma
terceira credencial — o código de acesso do signatário — passada por chamada, e não configurada no
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

## Métodos de verificação do signatário

Definidos por signatário ao criar o assignment. O método de verificação e o de notificação são
**acoplados**: envie um, os dois ou nenhum — o lado que faltar é inferido. Sem nenhum dos dois, ambos
assumem `Email`.

| Método | Como funciona | Custo por signatário |
| --- | --- | --- |
| `Email` *(padrão)* | Código de uso único (OTP) por e-mail, exigido antes de assinar | Gratuito |
| `Whatsapp` | Código de uso único (OTP) por WhatsApp | Verificação gratuita; notificação 0,45 crédito, só em planos pagos |
| `DigitalCertificate` | O signatário assina com o **próprio certificado ICP-Brasil (A1/A3)**, pela extensão de navegador Web PKI, gerando uma assinatura **PAdES qualificada** | 2 créditos |

Combinações permitidas: `Email` → notifica por `Email`; `Whatsapp` → notifica por `Whatsapp`;
`DigitalCertificate` → notifica por `Email` **ou** `Whatsapp`. Apenas um método de notificação por
signatário.

### Certificado digital ICP-Brasil

Exige o recurso **Certificado Digital** na conta (planos Standard e Pro), CPF ou CNPJ em
`governmentId` do signatário, e exatamente **um signatário por certificado naquele passo**. Um CPF
exige o certificado daquela pessoa (e-CPF, ou e-CNPJ que a nomeie como representante legal); um CNPJ
exige um e-CNPJ da empresa.

Estime o custo antes: a assinatura por certificado custa 2 créditos por signatário, além do custo da
notificação escolhida.

Antes de abrir o assignment, o signatário precisa confirmar os dados de identidade e aceitar os
termos. O endpoint comum de assinatura **rejeita** signatários por certificado — a assinatura deles é
produzida por um handshake de dois passos com a extensão Web PKI:

```
POST /v1/signers/certificate/start     → data.token   (token da operação Web PKI)
        ↓  o navegador assina o token com o certificado do signatário
POST /v1/signers/certificate/complete  → data.signerName
```

> Essas duas rotas são extensões implantadas **somente em produção**: o sandbox não as expõe e elas
> não constam do documento OpenAPI publicado.

Concluído o fluxo, baixar o artefato `pades` devolve a assinatura PAdES qualificada.

## Trilha de atividades e artefatos

As atividades de um documento devolvem todos os eventos registrados, cada um com um snapshot do
`payload` do evento e a `origin` da requisição (`ip`, `user-agent`).

Artefatos disponíveis para download:

| Artefato | Conteúdo |
| --- | --- |
| `original` | O PDF enviado, como recebido |
| `certificated` | O documento assinado, com a certificação da plataforma |
| `certificate-page` | Apenas a página de certificação |
| `pades` | Assinaturas ICP-Brasil dos signatários + caixa de certificação — só existe em documentos que tiveram signatários por certificado digital |
| `bundle` | Zip com `original`, `certificated` e `certificate-page`, mais o `pades` quando houver |

A verificação pública confere um documento assinado pelo hash da assinatura, sem autenticação.

## Ambientes

| | |
| --- | --- |
| Produção | `https://api.assinafy.com.br/v1` |
| Sandbox | `AssinafyClientOptions.SANDBOX_BASE_URL` |

O sandbox é gratuito e espelha a produção para testar a integração de ponta a ponta — com a exceção
das rotas de certificado digital, que existem apenas em produção.

## Documentação

- **[README.en.md](README.en.md)** — referência completa por operação, em inglês
- [docs/API_REFERENCE.md](docs/API_REFERENCE.md)
- [Documentação da API](https://api.assinafy.com.br/v1/docs)

## Licença

Distribuído sob a licença [MIT](LICENSE).
