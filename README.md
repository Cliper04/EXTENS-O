
Sistema de Gestão para Lanchonete Sabor & Arte

Este é um aplicativo Android desenvolvido em Kotlin para gestão de vendas e estoque da Lanchonete Sabor & Arte, seguindo os princípios da Clean Architecture e utilizando tecnologias modernas.

Características Principais

- ✅ Registro de vendas com cálculo automático de impostos (ISS 5% + ICMS 18%)
- ✅ Controle de estoque em tempo real
- ✅ Alertas preventivos para produtos próximos ao vencimento
- ✅ Interface acessível e inclusiva (WCAG AA)
- ✅ Funcionamento offline com sincronização automática
- ✅ Validações automáticas para redução de erros operacionais

## Arquitetura

O projeto segue a **Clean Architecture** com as seguintes camadas:

### Domain Layer
- **Entities**: Sale, Product, StockAlert
- **Use Cases**: RegisterSaleUseCase, GetSalesUseCase, etc.
- **Repository Interfaces**: Contratos para acesso a dados

### Data Layer
- **Repository Implementations**: Implementações concretas dos repositórios
- **Data Sources**: Firebase Firestore como fonte de dados

### Presentation Layer
- **ViewModels**: Gerenciamento de estado com MVVM
- **UI**: Interface construída com Jetpack Compose

## Stack Tecnológica

- **Linguagem**: Kotlin 1.9
- **UI**: Jetpack Compose 1.6
- **Arquitetura**: Clean Architecture + MVVM
- **Injeção de Dependência**: Hilt
- **Backend**: Firebase (Firestore, Auth, Crashlytics)
- **Programação Assíncrona**: Coroutinas + Flow
- **Testes**: JUnit, MockK, Espresso

## Configuração do Projeto

### Pré-requisitos

1. Android Studio Hedgehog | 2023.1.1 ou superior
2. JDK 8 ou superior
3. Conta no Firebase

### Configuração do Firebase

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Crie um novo projeto ou use um existente
3. Adicione um app Android ao projeto
4. Baixe o arquivo `google-services.json` e coloque na pasta `app/`
5. Configure o Firestore Database:
   - Vá para Firestore Database
   - Clique em "Criar banco de dados"
   - Escolha "Iniciar no modo de teste"
6. Configure o Authentication:
   - Vá para Authentication
   - Ative "Número de telefone" como método de login

### Instalação

1. Clone o repositório:
```bash
git clone https://github.com/seu-usuario/lanchonete-sabor-arte.git
cd lanchonete-sabor-arte
```

2. Abra o projeto no Android Studio

3. Adicione o arquivo `google-services.json` na pasta `app/`

4. Sync o projeto (Android Studio fará automaticamente)

5. Execute o projeto no dispositivo ou emulador

### Configuração das Regras de Segurança

Copie as regras do arquivo `firestore.rules` para o Firebase Console:

1. Vá para Firestore Database > Regras
2. Cole o conteúdo do arquivo `firestore.rules`
3. Publique as regras

## Estrutura do Projeto

```
app/
├── src/main/java/com/saborarte/lanchonete/
│   ├── data/
│   │   ├── repository/          # Implementações dos repositórios
│   │   └── remote/              # Fontes de dados remotas
│   ├── domain/
│   │   ├── entity/              # Entidades de negócio
│   │   ├── repository/          # Interfaces dos repositórios
│   │   └── usecase/             # Casos de uso
│   ├── presentation/
│   │   ├── ui/                  # Composables e telas
│   │   ├── viewmodel/           # ViewModels
│   │   └── theme/               # Configuração de tema
│   ├── di/                      # Módulos Hilt
│   └── MainActivity.kt
├── src/test/                    # Testes unitários
├── src/androidTest/             # Testes instrumentados
└── build.gradle
```

## Funcionalidades

### Módulo de Vendas
- Registro de vendas com validação automática
- Cálculo de impostos em tempo real
- Cálculo automático de troco
- Histórico de vendas

### Módulo de Estoque
- Visualização de produtos em estoque
- Atualização de quantidades
- Alertas de estoque baixo
- Alertas de vencimento próximo

### Sistema de Alertas
- Notificações para produtos vencendo em 7 dias
- Alertas de estoque baixo (≤ 5 unidades)
- Marcação de alertas como lidos

## Testes

### Executar Testes Unitários
```bash
./gradlew test
```

### Executar Testes Instrumentados
```bash
./gradlew connectedAndroidTest
```

### Cobertura de Testes
```bash
./gradlew jacocoTestReport
```

## CI/CD

O projeto inclui configuração para GitHub Actions com:
- Build automático
- Execução de testes
- Análise estática de código
- Deploy automático via Firebase App Distribution

## Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Contato

Projeto desenvolvido para a Lanchonete Sabor & Arte - Sete Lagoas/MG

- Email: 202303574746@alunos.estacio.br
- GitHub: [Cliper04/EXTENS-O](https://github.com/Cliper04/EXTENS-O)

