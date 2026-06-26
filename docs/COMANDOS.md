# Comandos e Permissoes

## Jogador

| Comando | Permissao | Descricao |
| --- | --- | --- |
| `/correio` | `focuscorreio.usar` | Abre o menu do correio. |
| `/correio resgatar` | `focuscorreio.usar` | Resgata tudo que couber no inventario. |
| `/correio ajuda` | `focuscorreio.usar` | Mostra a ajuda. |

## Staff

| Comando | Permissao | Descricao |
| --- | --- | --- |
| `/correio adicionar <jogador> <material> <quantia> <titulo...>` | `focuscorreio.admin` | Envia item por material. |
| `/correio enviar <jogador> <titulo...>` | `focuscorreio.admin` | Envia o item da mao. |
| `/correio listar <jogador>` | `focuscorreio.admin` | Mostra recompensas pendentes. |
| `/correio limpar <jogador>` | `focuscorreio.admin` | Limpa o correio de um jogador. |
| `/correio reload` | `focuscorreio.admin` | Recarrega os arquivos. |

## Dragao

| Comando | Permissao | Descricao |
| --- | --- | --- |
| `/correio dragao ver` | `focuscorreio.admin` | Mostra a recompensa configurada. |
| `/correio dragao definir <material> <quantia> <titulo...>` | `focuscorreio.admin` | Define e ativa a recompensa. |
| `/correio dragao ativar` | `focuscorreio.admin` | Ativa a recompensa automatica. |
| `/correio dragao desativar` | `focuscorreio.admin` | Desativa a recompensa automatica. |
| `/correio dragao dar <jogador>` | `focuscorreio.admin` | Envia a recompensa configurada para teste. |

## Exemplos Prontos

```text
/correio adicionar Notch DIAMOND 16 Premio do Evento
/correio adicionar Steve NETHERITE_INGOT 1 Premio Lendario
/correio enviar Alex Premio da Staff
/correio listar Steve
/correio limpar Steve
```
