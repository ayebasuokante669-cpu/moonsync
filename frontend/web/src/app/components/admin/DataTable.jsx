export function DataTable({ columns, data, onRowClick }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="bg-[var(--color-secondary-bg)] dark:bg-[#1E293B] border-b border-[var(--color-border)]">
          <tr>
            {columns.map((column, idx) => (
              <th
                key={idx}
                className="px-6 py-3 text-left text-xs font-semibold text-[var(--color-muted-foreground)] uppercase tracking-wider"
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-[var(--color-border)]">
          {data.map((row) => (
            <tr
              key={row.id}
              onClick={() => onRowClick && onRowClick(row)}
              className={`transition-smooth bg-transparent dark:bg-[#0F172A] ${onRowClick ? "cursor-pointer hover:bg-[var(--color-secondary-bg)] dark:hover:bg-[#1A2437]" : ""}`}
            >
              {columns.map((column, idx) => {
                const value =
                  typeof column.accessor === "function"
                    ? column.accessor(row)
                    : row[column.accessor];

                return (
                  <td key={idx} className={`px-6 py-4 whitespace-nowrap ${column.className || ""}`}>
                    {value}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
