import axios from 'axios';

interface ApiErrorBody {
  message?: string;
}

export interface ApiErrorContext {
  members?: Array<{ id: number; name: string }>;
}

const API_MESSAGE_TRANSLATIONS: Array<{ pattern: RegExp; message: string }> = [
  {
    pattern: /expected end date must be on or after start date/i,
    message: 'A previsão de término deve ser igual ou posterior à data de início.',
  },
  {
    pattern: /project cannot be modified while status is/i,
    message: 'Este projeto não pode ser alterado no status atual.',
  },
  {
    pattern: /only members with role 'manager'/i,
    message: 'Selecione um membro com perfil de gerente.',
  },
  {
    pattern: /only members with role 'employee'/i,
    message: 'Apenas funcionários podem ser alocados ao projeto.',
  },
  {
    pattern: /only employees can be allocated to projects/i,
    message: 'Apenas funcionários podem ser alocados ao projeto.',
  },
  {
    pattern: /must have at least .* allocated member/i,
    message: 'Selecione ao menos um membro para o projeto.',
  },
  {
    pattern: /can have at most .* allocated members/i,
    message: 'O projeto pode ter no máximo 10 membros.',
  },
  {
    pattern: /invalid status transition/i,
    message: 'Não é possível alterar o status do projeto para esta etapa.',
  },
  {
    pattern: /project is already in status/i,
    message: 'O projeto já está neste status.',
  },
  {
    pattern: /cannot cancel a project that is already/i,
    message: 'Não é possível cancelar o projeto neste status.',
  },
  {
    pattern: /project cannot be deleted while status is/i,
    message: 'Não é possível excluir o projeto neste status.',
  },
  {
    pattern: /invalid member role/i,
    message: 'Perfil de membro inválido.',
  },
  {
    pattern: /you do not have permission/i,
    message: 'Você não tem permissão para realizar esta ação.',
  },
  {
    pattern: /an unexpected error occurred/i,
    message: 'Ocorreu um erro inesperado. Tente novamente em instantes.',
  },
];

function translateMemberOverloadMessage(message: string, context?: ApiErrorContext): string | null {
  const idMatch = message.match(/Member id (\d+) is already allocated to (\d+) active projects/i);
  if (idMatch) {
    const [, id, count] = idMatch;
    const memberName = context?.members?.find((member) => member.id === Number(id))?.name;
    return `${memberName ?? 'Este membro'} já está alocado em ${count} projetos ativos. Escolha outro membro.`;
  }

  const namedMatch = message.match(/^(.+?) is already allocated to (\d+) active projects$/i);
  if (namedMatch) {
    const [, name, count] = namedMatch;
    return `${name} já está alocado em ${count} projetos ativos. Escolha outro membro.`;
  }

  return null;
}

function translateApiMessage(message: string, context?: ApiErrorContext): string | null {
  const normalized = message.trim();
  const memberOverload = translateMemberOverloadMessage(normalized, context);
  if (memberOverload) {
    return memberOverload;
  }

  const match = API_MESSAGE_TRANSLATIONS.find(({ pattern }) => pattern.test(normalized));
  return match?.message ?? null;
}

export function getApiErrorMessage(
  error: unknown,
  fallback: string,
  context?: ApiErrorContext,
): string {
  if (axios.isAxiosError<ApiErrorBody>(error)) {
    const apiMessage = error.response?.data?.message;
    if (apiMessage) {
      const translated = translateApiMessage(apiMessage, context);
      if (translated) {
        return translated;
      }
    }
  }

  return fallback;
}
